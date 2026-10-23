package org.propflow.back.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.propflow.back.service.ProposalDocumentRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
@RestController
@RequestMapping("/v1/api/pdf")
@Tag(name = "reports", description = "PDF report generation")
public class ReportController {

  @Autowired private ProposalDocumentRenderer renderer;

  @Operation(summary = "Download a generated proposal PDF")
  @GetMapping("/{id}")
  public ResponseEntity<byte[]> downloadPdf(
      @PathVariable("id") Long id,
      @RequestParam(value = "lang", required = false) String lang,
      @org.springframework.web.bind.annotation.RequestHeader(
              value = "Accept-Language",
              required = false)
          String acceptLanguage) {

    Locale locale = resolveLocale(lang, acceptLanguage);
    byte[] pdf = renderer.render(id, locale);
    String fileName = "proposal-" + id + ".pdf";

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
        .contentType(MediaType.APPLICATION_PDF)
        .body(pdf);
  }

  private Locale resolveLocale(String lang, String acceptLanguage) {
    String tag = lang;
    if (tag == null || tag.isBlank()) {
      tag = acceptLanguage;
    }
    if (tag == null || tag.isBlank()) {
      tag = "en";
    } else {
      tag = tag.split(",")[0].split(";")[0].trim();
      if (tag.contains("-")) {
        tag = tag.split("-")[0];
      }
    }
    tag = tag.toLowerCase();
    if (!tag.equals("es") && !tag.equals("pt") && !tag.equals("de") && !tag.equals("en")) {
      tag = "en";
    }
    return Locale.forLanguageTag(tag);
  }
}
