package org.propflow.back.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.propflow.back.model.Candidate;
import org.propflow.back.model.Office;
import org.propflow.back.model.Proposal;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

class MailSenderServiceTest {

  private JavaMailSender mailSender;
  private MailSenderService service;

  @BeforeEach
  void setUp() {
    mailSender = mock(JavaMailSender.class);
    SpringTemplateEngine templateEngine = mock(SpringTemplateEngine.class);
    service = new MailSenderService(mailSender);
    ProposalDocumentRenderer renderer = mock(ProposalDocumentRenderer.class);
    when(renderer.render(any(), any())).thenReturn("fake pdf content".getBytes());
    ReflectionTestUtils.setField(service, "proposalDocumentRenderer", renderer);
    ReflectionTestUtils.setField(service, "thymeleafTemplateEngine", templateEngine);
    ReflectionTestUtils.setField(service, "emailSender", "demo@propflow.local");
    when(templateEngine.process(eq("candidateOffer.html"), any(Context.class)))
        .thenReturn("<html><body><img src=\"cid:banner\" alt=\"banner\"></body></html>");
  }

  @Test
  void candidateOfferInlinesTheTrackedBannerAsJpegAndAttachesThePdf() throws Exception {
    when(mailSender.createMimeMessage())
        .thenReturn(new MimeMessage(Session.getInstance(new Properties())));

    Path pdf = Files.createTempFile("propflow-offer", ".pdf");
    Files.writeString(pdf, "fake pdf content");
    try {
      service.notifyCandidateByEmail(proposal(), "en");

      ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
      verify(mailSender).send(captor.capture());
      MimeMessage sent = captor.getValue();
      sent.saveChanges();
      MimeMultipart multipart = (MimeMultipart) sent.getContent();

      boolean inlineBannerAsJpeg = false;
      boolean pdfAttachment = false;
      for (MimeBodyPart part : leafParts(multipart)) {
        if (Part.INLINE.equalsIgnoreCase(part.getDisposition())
            && "banner".equals(contentId(part))
            && part.getContentType() != null
            && part.getContentType().startsWith("image/jpeg")) {
          inlineBannerAsJpeg = true;
        }
        if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
          pdfAttachment = true;
        }
      }

      assertThat(inlineBannerAsJpeg).as("inline banner part declared as image/jpeg").isTrue();
      assertThat(pdfAttachment).as("pdf attachment part").isTrue();
    } finally {
      Files.deleteIfExists(pdf);
    }
  }

  @Test
  void offerEmailRendersPortugueseTemplateWithRawAmounts() throws Exception {
    when(mailSender.createMimeMessage())
        .thenReturn(new MimeMessage(Session.getInstance(new Properties())));
    ReflectionTestUtils.setField(service, "thymeleafTemplateEngine", realTemplateEngine());
    ReflectionTestUtils.setField(service, "messageSource", testMessageSource());

    service.sendTemplateToCandidate(templateData("pt"));

    ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
    verify(mailSender).send(captor.capture());
    MimeMessage sent = captor.getValue();
    sent.saveChanges();
    MimeMultipart multipart = (MimeMultipart) sent.getContent();
    String html = htmlContent(multipart);

    assertThat(html)
        .contains(
            "Olá Ada,",
            "Tipo de Contrato: Contrato sem termo",
            "Remuneração Mensal: 1000.0€ ilíquidos mensais (14000.0€ ilíquidos anuais)");
  }

  private static SpringTemplateEngine realTemplateEngine() {
    ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
    resolver.setPrefix("templates/");
    resolver.setSuffix(".html");
    resolver.setTemplateMode(TemplateMode.HTML);
    resolver.setCharacterEncoding("UTF-8");

    SpringTemplateEngine engine = new SpringTemplateEngine();
    engine.setTemplateResolver(resolver);
    engine.setTemplateEngineMessageSource(testMessageSource());
    return engine;
  }

  private static ReloadableResourceBundleMessageSource testMessageSource() {
    ReloadableResourceBundleMessageSource messageSource =
        new ReloadableResourceBundleMessageSource();
    messageSource.setBasename("classpath:messages");
    messageSource.setDefaultEncoding("UTF-8");
    messageSource.setFallbackToSystemLocale(false);
    return messageSource;
  }

  private static Map<String, Object> templateData(String language) {
    Map<String, Object> candidate = new HashMap<>();
    candidate.put("name", "Ada");
    candidate.put("email", "ada@example.com");

    Map<String, Object> office = new HashMap<>();
    office.put("name", "Lisbon Oriente");

    Map<String, Object> proposal = new HashMap<>();
    proposal.put("description", "Backend Engineer");
    proposal.put("indefinite", true);
    proposal.put("startDate", "2025-01-15");
    proposal.put("office", office);
    proposal.put("candidate", candidate);

    Map<String, Object> data = new HashMap<>();
    data.put("proposal", proposal);
    data.put(
        "monthly",
        List.of(
            List.of("Gross Salary", 1000.0),
            List.of("Base Salary", 900.0),
            List.of("IHT", 100.0),
            List.of("Food Allowance", 5.5),
            List.of("Car Allowance", 200.0),
            List.of("Fuel Allowance", 50.0)));
    data.put(
        "yearly",
        List.of(
            List.of("Gross Salary", 14000.0),
            List.of("Base Salary", 12600.0),
            List.of("IHT", 1400.0),
            List.of("Food Allowance", 121.0),
            List.of("Car Allowance", 2800.0),
            List.of("Fuel Allowance", 600.0),
            List.of("Flex", 10.0)));
    data.put("language", language);
    return data;
  }

  private static String htmlContent(MimeMultipart multipart) throws Exception {
    for (MimeBodyPart part : leafParts(multipart)) {
      if (part.getContentType() != null && part.getContentType().startsWith("text/html")) {
        return (String) part.getContent();
      }
    }
    return null;
  }

  private static String contentId(MimeBodyPart part) throws Exception {
    String[] header = part.getHeader("Content-ID");
    if (header == null || header.length == 0) {
      return null;
    }
    String value = header[0];
    if (value.startsWith("<") && value.endsWith(">")) {
      return value.substring(1, value.length() - 1);
    }
    return value;
  }

  private static List<MimeBodyPart> leafParts(MimeMultipart multipart) throws Exception {
    List<MimeBodyPart> leaves = new ArrayList<>();
    for (int i = 0; i < multipart.getCount(); i++) {
      MimeBodyPart part = (MimeBodyPart) multipart.getBodyPart(i);
      if (part.isMimeType("multipart/*")) {
        leaves.addAll(leafParts((MimeMultipart) part.getContent()));
      } else {
        leaves.add(part);
      }
    }
    return leaves;
  }

  private static Proposal proposal() {
    Office office = new Office();
    office.setName("Lisbon");

    Candidate candidate = new Candidate();
    candidate.setName("Ada");
    candidate.setEmail("ada@example.com");

    Proposal proposal = new Proposal();
    proposal.setDescription("Backend Engineer");
    proposal.setRole("Backend Engineer");
    proposal.setOffice(office);
    proposal.setCandidate(candidate);
    proposal.setId(1);
    return proposal;
  }
}
