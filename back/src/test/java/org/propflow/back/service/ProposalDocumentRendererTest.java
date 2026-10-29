package org.propflow.back.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ProposalDocumentRendererTest {

  @ParameterizedTest
  @CsvSource(
      nullValues = "null",
      value = {
        "de, de",
        "pt-BR, pt",
        "ES, es",
        "'es-ES,es;q=0.9,en;q=0.8', es",
        "fr, en",
        "'', en",
        "null, en"
      })
  void localeForKeepsOnlySupportedLanguages(String tag, String expected) {
    assertThat(ProposalDocumentRenderer.localeFor(tag)).isEqualTo(Locale.forLanguageTag(expected));
  }
}
