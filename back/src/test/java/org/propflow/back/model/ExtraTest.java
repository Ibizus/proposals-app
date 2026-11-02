package org.propflow.back.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ExtraTest {

  @Test
  @SuppressWarnings("deprecation")
  void newExtrasKeepNonNullLegacyRatesForNotNullLegacyColumns() {
    ExtraTemplate template = new ExtraTemplate();
    template.setName("Meal");
    template.setAmount(10.0);

    assertThat(new Extra().getSsTaxrate()).isEqualTo(0.0);
    assertThat(new Extra().getIrsTaxrate()).isEqualTo(0.0);
    assertThat(new Extra(template).getSsTaxrate()).isEqualTo(0.0);
    assertThat(new Extra(template).getIrsTaxrate()).isEqualTo(0.0);
  }
}
