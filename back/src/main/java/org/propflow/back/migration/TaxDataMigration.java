package org.propflow.back.migration;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.propflow.back.model.Extra;
import org.propflow.back.model.ExtraTemplate;
import org.propflow.back.model.Salary;
import org.propflow.back.model.Tax;
import org.propflow.back.model.TaxBracket;
import org.propflow.back.model.TaxConfiguration;
import org.propflow.back.model.TaxType;
import org.propflow.back.repository.ExtraRepository;
import org.propflow.back.repository.ExtraTemplateRepository;
import org.propflow.back.repository.SalaryRepository;
import org.propflow.back.repository.TaxConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * One-time migration from the old country-specific tax model to configurable tax configurations.
 *
 * <p>Runs on every startup but is idempotent: it only works when the new {@code tax_configuration}
 * table is empty. If the legacy {@code taxes} table still exists, it converts the unmarried/0-
 * dependents Portuguese brackets into a Portugal configuration, maps existing extras and templates
 * to reference the Portuguese taxes, and pins existing salaries to that Portugal configuration. It
 * always creates an illustrative Spain configuration as the active default.
 */
@Slf4j
@Component
@Order(1)
public class TaxDataMigration implements CommandLineRunner {

  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private TaxConfigurationRepository taxConfigurationRepository;
  @Autowired private ExtraRepository extraRepository;
  @Autowired private ExtraTemplateRepository extraTemplateRepository;
  @Autowired private SalaryRepository salaryRepository;

  @Override
  @Transactional
  public void run(String... args) {
    if (taxConfigurationRepository.count() > 0) {
      log.info("Tax configurations already exist; skipping tax data migration");
      return;
    }

    log.info("Creating default Spain tax configuration");
    TaxConfiguration spain = buildSpainConfiguration();

    if (!legacyTaxesTableExists()) {
      taxConfigurationRepository.save(spain);
      log.info("No legacy taxes table found; Spain configuration created as active default");
      return;
    }

    log.info("Migrating legacy tax data to configurable tax configurations");
    TaxConfiguration portugal = buildPortugalConfiguration(this::loadLegacyBrackets);
    Tax socialSecurity = portugal.getTaxes().get(0);
    Tax incomeTax = portugal.getTaxes().get(1);
    List<Extra> extras = extraRepository.findAll();
    List<ExtraTemplate> templates = extraTemplateRepository.findAll();
    Map<String, Tax> rateTaxes = new LinkedHashMap<>();
    for (HasLegacyTaxFlags entity : concat(extras, templates)) {
      if (Boolean.TRUE.equals(entity.getIsSSTaxable()) && entity.getSsTaxrate() != null) {
        rateTax(portugal, rateTaxes, socialSecurity.getName(), entity.getSsTaxrate());
      }
      if (Boolean.TRUE.equals(entity.getIsIRSTaxable()) && entity.getIrsTaxrate() != null) {
        rateTax(portugal, rateTaxes, incomeTax.getName(), entity.getIrsTaxrate());
      }
    }
    taxConfigurationRepository.saveAll(List.of(spain, portugal));

    TaxIds taxIds = new TaxIds(socialSecurity, incomeTax, rateTaxes);
    migrateExtras(extras, taxIds);
    migrateExtraTemplates(templates, taxIds, portugal.getId());
    migrateSalaries(portugal.getId());
    log.info(
        "Tax migration complete: Portugal configuration id={}, Spain configuration id={}",
        portugal.getId(),
        spain.getId());
  }

  private boolean legacyTaxesTableExists() {
    try {
      Long count =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'taxes' AND"
                  + " table_schema = DATABASE()",
              Long.class);
      return count != null && count > 0;
    } catch (DataAccessException e) {
      return false;
    }
  }

  public static TaxConfiguration buildSpainConfiguration() {
    TaxConfiguration config = new TaxConfiguration();
    config.setCountryCode("es");
    config.setLabel("Spain (illustrative example data)");
    config.setYearlyPayments(12);
    config.setActive(true);

    Tax socialSecurity = new Tax();
    socialSecurity.setName("Social Security");
    socialSecurity.setType(TaxType.FIXED);
    socialSecurity.setFixedRate(6.45);
    socialSecurity.setConfiguration(config);

    Tax irpf = new Tax();
    irpf.setName("IRPF");
    irpf.setType(TaxType.BRACKETED);
    irpf.setConfiguration(config);
    irpf.setBrackets(
        List.of(
            bracket(irpf, 1200, 0.0),
            bracket(irpf, 1500, 8.0),
            bracket(irpf, 2000, 12.0),
            bracket(irpf, 3000, 17.0),
            bracket(irpf, 4000, 24.0),
            bracket(irpf, 6000, 30.0),
            bracket(irpf, 9999999, 37.0)));

    config.setTaxes(new ArrayList<>(List.of(socialSecurity, irpf)));
    return config;
  }

  public static TaxConfiguration buildPortugalConfiguration(
      Function<Tax, List<TaxBracket>> irsBrackets) {
    TaxConfiguration config = new TaxConfiguration();
    config.setCountryCode("pt");
    config.setLabel("Portugal (illustrative example data)");
    config.setYearlyPayments(14);
    config.setActive(false);

    Tax socialSecurity = new Tax();
    socialSecurity.setName("Social Security");
    socialSecurity.setType(TaxType.FIXED);
    socialSecurity.setFixedRate(11.0);
    socialSecurity.setConfiguration(config);

    Tax irs = new Tax();
    irs.setName("IRS");
    irs.setType(TaxType.WITHHOLDING);
    irs.setConfiguration(config);
    irs.setBrackets(irsBrackets.apply(irs));

    config.setTaxes(new ArrayList<>(List.of(socialSecurity, irs)));
    return config;
  }

  private List<TaxBracket> loadLegacyBrackets(Tax irs) {
    List<TaxBracket> brackets = new ArrayList<>();
    try {
      jdbcTemplate.query(
          "SELECT salary_max, zero FROM taxes WHERE marital_status = 0 ORDER BY salary_max ASC",
          (ResultSet rs) -> {
            brackets.add(bracket(irs, rs.getDouble("salary_max"), rs.getDouble("zero")));
          });
    } catch (DataAccessException e) {
      log.warn("Could not read legacy taxes table: {}", e.getMessage());
    }
    return brackets;
  }

  public static TaxBracket bracket(Tax tax, double threshold, double rate) {
    TaxBracket bracket = new TaxBracket();
    bracket.setThreshold(threshold);
    bracket.setRate(rate);
    bracket.setTax(tax);
    return bracket;
  }

  private static List<HasLegacyTaxFlags> concat(List<Extra> extras, List<ExtraTemplate> templates) {
    List<HasLegacyTaxFlags> all = new ArrayList<>(extras);
    all.addAll(templates);
    return all;
  }

  private static String rateKey(String kind, double rate) {
    return kind + ":" + rate;
  }

  private static void rateTax(
      TaxConfiguration config, Map<String, Tax> rateTaxes, String kind, double rate) {
    rateTaxes.computeIfAbsent(
        rateKey(kind, rate),
        key -> {
          Tax tax = new Tax();
          tax.setName(
              kind
                  + " "
                  + BigDecimal.valueOf(rate).stripTrailingZeros().toPlainString()
                  + "% (extras)");
          tax.setType(TaxType.FIXED);
          tax.setFixedRate(rate);
          tax.setExtrasOnly(true);
          tax.setConfiguration(config);
          config.getTaxes().add(tax);
          return tax;
        });
  }

  private record TaxIds(Tax socialSecurity, Tax incomeTax, Map<String, Tax> rateTaxes) {

    long socialSecurityId(Double rate) {
      return rate == null
          ? socialSecurity.getId()
          : rateTaxes.get(rateKey(socialSecurity.getName(), rate)).getId();
    }

    long incomeTaxId(Double rate) {
      return rate == null
          ? incomeTax.getId()
          : rateTaxes.get(rateKey(incomeTax.getName(), rate)).getId();
    }
  }

  private void migrateExtras(List<Extra> extras, TaxIds taxIds) {
    for (Extra extra : extras) {
      applyTaxApplicability(extra, taxIds);
      // Legacy columns are deprecated but may still be NOT NULL in existing databases, so keep
      // harmless defaults instead of writing null.
      extra.setIsSSTaxable(false);
      extra.setIsIRSTaxable(false);
      extra.setSsTaxrate(0.0);
      extra.setIrsTaxrate(0.0);
    }
    extraRepository.saveAll(extras);
    log.info("Migrated {} extras to reference configurable taxes", extras.size());
  }

  private void migrateExtraTemplates(
      List<ExtraTemplate> templates, TaxIds taxIds, long portugalConfigurationId) {
    for (ExtraTemplate template : templates) {
      applyTaxApplicability(template, taxIds);
      template.setTaxConfigurationId(portugalConfigurationId);
      template.setIsSSTaxable(false);
      template.setIsIRSTaxable(false);
      template.setSsTaxrate(0.0);
      template.setIrsTaxrate(0.0);
    }
    extraTemplateRepository.saveAll(templates);
    log.info("Migrated {} extra templates to reference configurable taxes", templates.size());
  }

  private void applyTaxApplicability(HasLegacyTaxFlags entity, TaxIds taxIds) {
    Set<Long> ids = new HashSet<>();
    if (Boolean.TRUE.equals(entity.getIsSSTaxable())) {
      ids.add(taxIds.socialSecurityId(entity.getSsTaxrate()));
    }
    if (Boolean.TRUE.equals(entity.getIsIRSTaxable())) {
      ids.add(taxIds.incomeTaxId(entity.getIrsTaxrate()));
    }
    entity.setTaxIds(ids);
    entity.setExempt(ids.isEmpty());
  }

  private void migrateSalaries(long portugalConfigurationId) {
    List<Salary> salaries = salaryRepository.findAll();
    for (Salary salary : salaries) {
      salary.setTaxConfigurationId(portugalConfigurationId);
    }
    salaryRepository.saveAll(salaries);
    log.info("Pinned {} salaries to the Portugal tax configuration", salaries.size());
  }
}
