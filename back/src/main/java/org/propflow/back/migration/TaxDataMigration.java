package org.propflow.back.migration;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    TaxConfiguration portugal = buildPortugalConfigurationFromLegacy();
    taxConfigurationRepository.saveAll(List.of(spain, portugal));

    long socialSecurityId =
        portugal.getTaxes().stream()
            .filter(t -> "Social Security".equals(t.getName()))
            .findFirst()
            .orElseThrow()
            .getId();
    long incomeTaxId =
        portugal.getTaxes().stream()
            .filter(t -> "IRS".equals(t.getName()))
            .findFirst()
            .orElseThrow()
            .getId();

    migrateExtras(socialSecurityId, incomeTaxId);
    migrateExtraTemplates(socialSecurityId, incomeTaxId);
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
              "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'taxes'",
              Long.class);
      return count != null && count > 0;
    } catch (DataAccessException e) {
      return false;
    }
  }

  private TaxConfiguration buildSpainConfiguration() {
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

    config.setTaxes(List.of(socialSecurity, irpf));
    return config;
  }

  private TaxConfiguration buildPortugalConfigurationFromLegacy() {
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
    irs.setType(TaxType.BRACKETED);
    irs.setConfiguration(config);
    irs.setBrackets(loadLegacyBrackets(irs));

    config.setTaxes(List.of(socialSecurity, irs));
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

  private TaxBracket bracket(Tax tax, double threshold, double rate) {
    TaxBracket bracket = new TaxBracket();
    bracket.setThreshold(threshold);
    bracket.setRate(rate);
    bracket.setTax(tax);
    return bracket;
  }

  private void migrateExtras(long socialSecurityId, long incomeTaxId) {
    List<Extra> extras = extraRepository.findAll();
    for (Extra extra : extras) {
      applyTaxApplicability(extra, socialSecurityId, incomeTaxId);
      extra.setIsSSTaxable(null);
      extra.setIsIRSTaxable(null);
      extra.setSsTaxrate(null);
      extra.setIrsTaxrate(null);
    }
    extraRepository.saveAll(extras);
    log.info("Migrated {} extras to reference configurable taxes", extras.size());
  }

  private void migrateExtraTemplates(long socialSecurityId, long incomeTaxId) {
    List<ExtraTemplate> templates = extraTemplateRepository.findAll();
    for (ExtraTemplate template : templates) {
      applyTaxApplicability(template, socialSecurityId, incomeTaxId);
      template.setIsSSTaxable(null);
      template.setIsIRSTaxable(null);
      template.setSsTaxrate(null);
      template.setIrsTaxrate(null);
    }
    extraTemplateRepository.saveAll(templates);
    log.info("Migrated {} extra templates to reference configurable taxes", templates.size());
  }

  private <T extends HasLegacyTaxFlags> void applyTaxApplicability(
      T entity, long socialSecurityId, long incomeTaxId) {
    Set<Long> taxIds = new HashSet<>();
    if (Boolean.TRUE.equals(entity.getIsSSTaxable())) {
      taxIds.add(socialSecurityId);
    }
    if (Boolean.TRUE.equals(entity.getIsIRSTaxable())) {
      taxIds.add(incomeTaxId);
    }
    entity.setTaxIds(taxIds);
    entity.setExempt(taxIds.isEmpty());
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
