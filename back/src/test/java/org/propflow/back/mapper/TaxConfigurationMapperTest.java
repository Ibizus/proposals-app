package org.propflow.back.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.propflow.back.dto.TaxBracketDTO;
import org.propflow.back.dto.TaxConfigurationDTO;
import org.propflow.back.dto.TaxDTO;
import org.propflow.back.model.Tax;
import org.propflow.back.model.TaxConfiguration;
import org.propflow.back.model.TaxType;

class TaxConfigurationMapperTest {

  private final TaxConfigurationMapper mapper = new TaxConfigurationMapper();

  @Test
  void toEntityIgnoresClientSuppliedIdsSoCreateNeverReplacesExistingRows() {
    TaxConfigurationDTO dto =
        new TaxConfigurationDTO(
            1L,
            "es",
            "Spain",
            12,
            true,
            List.of(
                new TaxDTO(
                    5L,
                    "IRPF",
                    TaxType.BRACKETED,
                    null,
                    false,
                    List.of(new TaxBracketDTO(9L, 1000.0, 10.0)))));

    TaxConfiguration entity = mapper.toEntity(dto);

    assertThat(entity.getId()).isZero();
    Tax tax = entity.getTaxes().get(0);
    assertThat(tax.getId()).isZero();
    assertThat(tax.getBrackets().get(0).getId()).isZero();
  }
}
