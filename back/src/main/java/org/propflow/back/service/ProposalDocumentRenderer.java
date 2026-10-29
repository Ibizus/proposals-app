package org.propflow.back.service;

import java.util.Locale;
import java.util.Set;

public interface ProposalDocumentRenderer {
  Set<String> SUPPORTED_LANGUAGES = Set.of("es", "pt", "de", "en");

  byte[] render(Long proposalId, Locale locale);

  static Locale localeFor(String tag) {
    if (tag == null || tag.isBlank()) {
      return Locale.ENGLISH;
    }
    String language = tag.split(",")[0].split(";")[0].trim().split("[-_]")[0].toLowerCase();
    return SUPPORTED_LANGUAGES.contains(language)
        ? Locale.forLanguageTag(language)
        : Locale.ENGLISH;
  }
}
