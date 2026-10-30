package org.propflow.back.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.propflow.back.model.Tax;
import org.propflow.back.model.TaxBracket;
import org.propflow.back.model.TaxConfiguration;
import org.propflow.back.model.TaxType;
import org.propflow.back.repository.TaxConfigurationRepository;

@ExtendWith(MockitoExtension.class)
class TaxConfigurationServiceTest {

  @Mock private TaxConfigurationRepository taxConfigurationRepository;

  @InjectMocks private TaxConfigurationService service;

  private static TaxConfiguration config(long id, String country, boolean active) {
    TaxConfiguration c = new TaxConfiguration();
    c.setId(id);
    c.setCountryCode(country);
    c.setYearlyPayments(12);
    c.setActive(active);
    c.setTaxes(new ArrayList<>());
    return c;
  }

  @Test
  void findsActiveConfiguration() {
    TaxConfiguration active = config(1, "es", true);
    when(taxConfigurationRepository.findByActiveTrue()).thenReturn(Optional.of(active));

    assertThat(service.findActive()).isSameAs(active);
  }

  @Test
  void missingActiveConfigurationThrows() {
    when(taxConfigurationRepository.findByActiveTrue()).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.findActive()).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void activateMakesTargetActiveAndOthersInactive() {
    TaxConfiguration spain = config(1, "es", true);
    TaxConfiguration portugal = config(2, "pt", false);
    when(taxConfigurationRepository.findById(2L)).thenReturn(Optional.of(portugal));
    when(taxConfigurationRepository.findAll()).thenReturn(List.of(spain, portugal));

    TaxConfiguration result = service.activate(2L);

    assertThat(result.getActive()).isTrue();
    assertThat(spain.getActive()).isFalse();
  }

  @Test
  void saveWiresChildrenAndClearsUnusedFields() {
    TaxConfiguration c = config(0, "es", true);
    Tax fixed = new Tax();
    fixed.setName("SS");
    fixed.setType(TaxType.FIXED);
    fixed.setFixedRate(10.0);
    fixed.setBrackets(List.of(new TaxBracket()));
    c.setTaxes(List.of(fixed));

    when(taxConfigurationRepository.save(c)).thenReturn(c);

    TaxConfiguration saved = service.save(c);

    assertThat(saved.getTaxes().get(0).getConfiguration()).isSameAs(saved);
    assertThat(saved.getTaxes().get(0).getBrackets()).isNull();
  }

  @Test
  void saveActivatingConfigurationDeactivatesOthers() {
    TaxConfiguration spain = config(1, "es", true);
    TaxConfiguration portugal = config(2, "pt", false);
    when(taxConfigurationRepository.findAll()).thenReturn(List.of(spain, portugal));
    when(taxConfigurationRepository.save(portugal)).thenReturn(portugal);

    portugal.setActive(true);
    TaxConfiguration saved = service.save(portugal);

    assertThat(saved.getActive()).isTrue();
    assertThat(spain.getActive()).isFalse();
  }
}
