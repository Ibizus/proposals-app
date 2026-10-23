package org.propflow.back.service;

import com.openhtmltopdf.pdfboxout.PDFontSupplier;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder.PdfAConformance;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.propflow.back.model.Proposal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class OpenHtmlToPdfProposalDocumentRenderer implements ProposalDocumentRenderer {

  private static final String TEMPLATE = "proposalDocument";

  @Autowired private ProposalService proposalService;
  @Autowired private SalaryService salaryService;
  @Autowired private TemplateEngine templateEngine;
  @Autowired private MessageSource messageSource;

  @Override
  public byte[] render(Long proposalId, Locale locale) {
    Proposal proposal = proposalService.one(proposalId);
    Map<String, Map<String, Double>> calculations =
        salaryService.salaryCalculations(proposal, null, null);

    Map<String, Double> monthly = calculations.get("monthly");
    Map<String, Double> yearly = calculations.get("yearly");

    List<List<Object>> monthlyRows = toRows(monthly);
    List<List<Object>> yearlyRows = toRows(yearly);

    Context context = new Context(locale);
    context.setVariable("proposal", proposal);
    context.setVariable("monthly", monthlyRows);
    context.setVariable("yearly", yearlyRows);
    context.setVariable("messageSource", messageSource);

    String html = templateEngine.process(TEMPLATE, context);

    try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
      PdfRendererBuilder builder = new PdfRendererBuilder();
      builder.useFastMode();
      builder.usePdfAConformance(PdfAConformance.PDFA_2_U);
      try (var in = new ClassPathResource("fonts/NotoSans-Regular.ttf").getInputStream()) {
        var doc = new PDDocument();
        var font = PDType0Font.load(doc, in, true);
        builder.useFont(new PDFontSupplier(font), "Noto Sans");
      }
      try (var in = new ClassPathResource("fonts/NotoSans-Bold.ttf").getInputStream()) {
        var doc = new PDDocument();
        var font = PDType0Font.load(doc, in, true);
        builder.useFont(
            new PDFontSupplier(font),
            "Noto Sans",
            700,
            com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.FontStyle.NORMAL,
            false);
      }
      builder.withHtmlContent(html, null);
      builder.toStream(os);
      builder.run();
      return os.toByteArray();
    } catch (Exception e) {
      throw new RuntimeException("Could not render PDF", e);
    }
  }

  private List<List<Object>> toRows(Map<String, Double> map) {
    List<List<Object>> rows = new ArrayList<>();
    if (map == null) {
      return rows;
    }
    for (Map.Entry<String, Double> e : map.entrySet()) {
      List<Object> row = new ArrayList<>();
      row.add(e.getKey());
      row.add(e.getValue());
      rows.add(row);
    }
    return rows;
  }
}
