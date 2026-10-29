package org.propflow.back.service;

import com.openhtmltopdf.pdfboxout.PDFontSupplier;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder.PdfAConformance;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_Profile;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.propflow.back.model.Proposal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class OpenHtmlToPdfProposalDocumentRenderer implements ProposalDocumentRenderer {

  private static final String TEMPLATE = "proposalDocument";
  private static final String LOGO_PATH = "static/img/logo.png";
  private static final String LOGO_MIME = "image/png";

  @Autowired private ProposalService proposalService;
  @Autowired private SalaryService salaryService;
  @Autowired private TemplateEngine templateEngine;

  @Override
  @Transactional(readOnly = true)
  public byte[] render(Long proposalId, Locale locale) {
    Proposal proposal = proposalService.one(proposalId);
    Map<String, Map<String, Double>> calculations =
        salaryService.salaryCalculations(proposal, null, null);

    Map<String, Double> monthly = calculations.get("monthly");
    Map<String, Double> yearly = calculations.get("yearly");

    List<Map<String, Object>> monthlyRows = toRows(monthly);
    List<Map<String, Object>> yearlyRows = toRows(yearly);

    Context context = new Context(locale);
    context.setVariable("proposal", proposal);
    context.setVariable("monthly", monthlyRows);
    context.setVariable("yearly", yearlyRows);
    context.setVariable("logoDataUri", logoDataUri());

    String html = templateEngine.process(TEMPLATE, context);

    try (ByteArrayOutputStream os = new ByteArrayOutputStream();
        PDDocument fontDoc = new PDDocument()) {
      PdfRendererBuilder builder = new PdfRendererBuilder();
      builder.useFastMode();
      builder.usePdfAConformance(PdfAConformance.PDFA_2_U);
      builder.useColorProfile(ICC_Profile.getInstance(ColorSpace.CS_sRGB).getData());
      try (var in = new ClassPathResource("fonts/NotoSans-Regular.ttf").getInputStream()) {
        var font = PDType0Font.load(fontDoc, in, true);
        builder.useFont(new PDFontSupplier(font), "Noto Sans");
      }
      try (var in = new ClassPathResource("fonts/NotoSans-Bold.ttf").getInputStream()) {
        var font = PDType0Font.load(fontDoc, in, true);
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

  private String logoDataUri() {
    try (InputStream in = new ClassPathResource(LOGO_PATH).getInputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      in.transferTo(out);
      return "data:"
          + LOGO_MIME
          + ";base64,"
          + Base64.getEncoder().encodeToString(out.toByteArray());
    } catch (IOException e) {
      throw new RuntimeException("Could not load logo image", e);
    }
  }

  private List<Map<String, Object>> toRows(Map<String, Double> map) {
    List<Map<String, Object>> rows = new ArrayList<>();
    if (map == null) {
      return rows;
    }
    for (Map.Entry<String, Double> e : map.entrySet()) {
      Map<String, Object> row = new HashMap<>();
      row.put("labelKey", labelKey(e.getKey()));
      row.put("rawLabel", e.getKey());
      row.put("amount", e.getValue());
      rows.add(row);
    }
    return rows;
  }

  private static String labelKey(String raw) {
    return switch (raw) {
      case "Gross Salary" -> "grossSalary";
      case "Base Salary" -> "baseSalary";
      case "IHT" -> "iht";
      case "Share Purchase Plan" -> "sharePurchasePlan";
      case "Estimated Net Salary" -> "estimatedNetSalary";
      case "Food Allowance" -> "foodAllowance";
      case "Car Allowance" -> "carAllowance";
      case "Fuel Allowance" -> "fuelAllowance";
      case "Flex" -> "flex";
      default -> raw;
    };
  }
}
