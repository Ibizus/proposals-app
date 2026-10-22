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
import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.propflow.back.model.Candidate;
import org.propflow.back.model.Office;
import org.propflow.back.model.Proposal;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

class MailSenderServiceTest {

  private JavaMailSender mailSender;
  private MailSenderService service;

  @BeforeEach
  void setUp() {
    mailSender = mock(JavaMailSender.class);
    SpringTemplateEngine templateEngine = mock(SpringTemplateEngine.class);
    service = new MailSenderService(mailSender);
    ReflectionTestUtils.setField(service, "thymeleafTemplateEngine", templateEngine);
    ReflectionTestUtils.setField(service, "emailSenderUser", "demo@propflow.local");
    ReflectionTestUtils.setField(service, "attachPath", "target/test-uploads");
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
      service.notifyCandidateByEmail(proposal(), pdf.toString());

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
    return proposal;
  }
}
