package org.propflow.back.service;

import java.util.Locale;

public interface ProposalDocumentRenderer {
  byte[] render(Long proposalId, Locale locale);
}
