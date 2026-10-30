package org.propflow.back.migration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.propflow.back.model.Extra;
import org.propflow.back.model.ExtraTemplate;
import org.propflow.back.model.Tax;
import org.propflow.back.model.TaxConfiguration;
import org.propflow.back.model.TaxType;
import org.propflow.back.repository.ExtraRepository;
import org.propflow.back.repository.ExtraTemplateRepository;
import org.propflow.back.repository.SalaryRepository;
import org.propflow.back.repository.TaxConfigurationRepository;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class TaxDataMigrationTest {

  @Mock private JdbcTemplate jdbcTemplate;
  @Mock private TaxConfigurationRepository taxConfigurationRepository;
  @Mock private ExtraRepository extraRepository;
  @Mock private ExtraTemplateRepository extraTemplateRepository;
  @Mock private SalaryRepository salaryRepository;

  @InjectMocks private TaxDataMigration migration;

  @Captor private ArgumentCaptor<Iterable<TaxConfiguration>> savedConfigurations;

  @BeforeEach
  void setUp() {
    when(taxConfigurationRepository.count()).thenReturn(0L);
    when(jdbcTemplate.queryForObject(anyString(), eq(Long.class))).thenReturn(1L);
    AtomicLong ids = new AtomicLong(1);
    when(taxConfigurationRepository.saveAll(savedConfigurations.capture()))
        .thenAnswer(
            invocation -> {
              List<TaxConfiguration> configs = invocation.getArgument(0);
              for (TaxConfiguration config : configs) {
                config.setId(ids.getAndIncrement());
                for (Tax tax : config.getTaxes()) {
                  tax.setId(ids.getAndIncrement());
                }
              }
              return configs;
            });
    when(salaryRepository.findAll()).thenReturn(List.of());
    when(salaryRepository.saveAll(anyIterable())).thenReturn(List.of());
  }

  private static Extra extra(Double ssRate, Double irsRate) {
    Extra extra = new Extra();
    extra.setIsSSTaxable(true);
    extra.setIsIRSTaxable(true);
    extra.setSsTaxrate(ssRate);
    extra.setIrsTaxrate(irsRate);
    return extra;
  }

  private TaxConfiguration portugal() {
    for (TaxConfiguration config : savedConfigurations.getValue()) {
      if ("pt".equals(config.getCountryCode())) {
        return config;
      }
    }
    throw new AssertionError("Portugal configuration not saved");
  }

  private static Tax tax(TaxConfiguration config, String name) {
    return config.getTaxes().stream()
        .filter(t -> name.equals(t.getName()))
        .findFirst()
        .orElseThrow(() -> new AssertionError(name + " not found"));
  }

  @Test
  void legacyPerExtraRatesBecomeExtrasOnlyFixedTaxes() {
    Extra withRates = extra(11.0, 21.0);
    ExtraTemplate template = new ExtraTemplate();
    template.setIsSSTaxable(false);
    template.setIsIRSTaxable(true);
    template.setIrsTaxrate(21.0);
    when(extraRepository.findAll()).thenReturn(List.of(withRates));
    when(extraTemplateRepository.findAll()).thenReturn(List.of(template));

    migration.run();

    TaxConfiguration portugal = portugal();
    Tax ssRate = tax(portugal, "Social Security 11% (extras)");
    Tax irsRate = tax(portugal, "IRS 21% (extras)");
    assertThat(portugal.getTaxes()).hasSize(4);
    assertThat(ssRate.getType()).isEqualTo(TaxType.FIXED);
    assertThat(ssRate.getFixedRate()).isEqualTo(11.0);
    assertThat(ssRate.getExtrasOnly()).isTrue();
    assertThat(irsRate.getFixedRate()).isEqualTo(21.0);
    assertThat(irsRate.getExtrasOnly()).isTrue();
    assertThat(withRates.getTaxIds()).containsExactlyInAnyOrder(ssRate.getId(), irsRate.getId());
    assertThat(template.getTaxIds()).containsExactly(irsRate.getId());
    assertThat(template.getTaxConfigurationId()).isEqualTo(portugal.getId());
  }

  @Test
  void extrasWithoutLegacyRatesKeepReferencingTheBaseTaxes() {
    Extra withoutRates = extra(null, null);
    when(extraRepository.findAll()).thenReturn(List.of(withoutRates));
    when(extraTemplateRepository.findAll()).thenReturn(List.of());

    migration.run();

    TaxConfiguration portugal = portugal();
    assertThat(portugal.getTaxes()).hasSize(2);
    assertThat(withoutRates.getTaxIds())
        .containsExactlyInAnyOrder(
            tax(portugal, "Social Security").getId(), tax(portugal, "IRS").getId());
    assertThat(withoutRates.getExempt()).isFalse();
  }
}
