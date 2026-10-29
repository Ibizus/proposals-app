package org.propflow.back.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.propflow.back.model.Proposal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
public class MailSenderService {

  private static final String BANNER_CONTENT_ID = "banner";
  private static final String BANNER_CONTENT_TYPE = "image/jpeg";
  private static final String BANNER_FILENAME = "banner.jpeg";
  private static final String BANNER_CLASSPATH = "static/img/" + BANNER_FILENAME;

  private static final String OFFER_TEMPLATE = "proposal.html";
  private static final String CANDIDATE_OFFER_TEMPLATE = "candidateOffer.html";

  @Value("${app.mail.from}")
  private String emailSender;

  @Autowired private SpringTemplateEngine thymeleafTemplateEngine;
  @Autowired private JavaMailSender mailSender;
  @Autowired private ProposalService proposalService;
  @Autowired private ProposalDocumentRenderer proposalDocumentRenderer;

  public MailSenderService(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  // Returns the tracked classpath banner image to inline in outgoing mail.
  private InputStreamSource bannerResource() {
    return new ClassPathResource(BANNER_CLASSPATH);
  }

  // SIMPLE SEND:
  public void sendEmail(String to, String subject, String body) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(to);
    message.setSubject(subject);
    message.setText(body);
    mailSender.send(message);
  }

  public void send(String from, String to, String subject, String text) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(from);
    message.setTo(to);
    message.setSubject(subject);
    message.setText(text);
    mailSender.send(message);
  }

  public void sendWithInline(
      String from,
      String to,
      String subject,
      String text,
      String contentIdBanner,
      InputStreamSource inputStreamBanner,
      String contentTypeBanner)
      throws MessagingException {
    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true);
    helper.setFrom(from);
    helper.setTo(to);
    helper.setSubject(subject);
    helper.setText(text, true);
    helper.addInline(contentIdBanner, inputStreamBanner, contentTypeBanner);
    mailSender.send(message);
  }

  public void sendWithAttach(
      String from,
      String to,
      String subject,
      String text,
      String contentIdBanner,
      InputStreamSource inputStreamBanner,
      String contentTypeBanner,
      String attachName,
      InputStreamSource inputStreamFile)
      throws MessagingException {
    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true);
    helper.setFrom(from);
    helper.setTo(to);
    helper.setSubject(subject);
    helper.setText(text, true);
    helper.addInline(contentIdBanner, inputStreamBanner, contentTypeBanner);
    helper.addAttachment(attachName, inputStreamFile);
    mailSender.send(message);
  }

  // SEND PROPOSAL BUILDING EMAIL FROM A FRONTEND FORM:
  @Async
  public void sendProposalTo(
      String to, String subject, String body, Long proposalId, String language) {

    Map<String, Object> templateModel = new HashMap<>();
    templateModel.put("body", body);
    Locale locale = ProposalDocumentRenderer.localeFor(language);
    Context thymeleafContext = new Context(locale);
    thymeleafContext.setVariables(templateModel);
    String htmlBody = thymeleafTemplateEngine.process(OFFER_TEMPLATE, thymeleafContext);

    byte[] attachment = proposalDocumentRenderer.render(proposalId, locale);
    String fileName = "proposal-" + proposalId + ".pdf";

    try {
      this.sendWithAttach(
          emailSender,
          to,
          subject,
          htmlBody,
          BANNER_CONTENT_ID,
          bannerResource(),
          BANNER_CONTENT_TYPE,
          fileName,
          new ByteArrayResource(attachment));
    } catch (MessagingException e) {
      throw new RuntimeException("Error sending email: " + e);
    }
  }

  // USES CANDIDATE-OFFER TEMPLATE:
  @Async
  public void notifyCandidateByEmail(Proposal proposal, String language) {

    Proposal resolved = proposalService.one(proposal.getId());

    String subject = "PropFlow " + resolved.getDescription();
    Locale locale = ProposalDocumentRenderer.localeFor(language);
    Map<String, Object> templateModel = new HashMap<>();
    templateModel.put("candidate", resolved.getCandidate().getName());
    templateModel.put("role", resolved.getRole());
    templateModel.put("office", resolved.getOffice().getName());

    Context thymeleafContext = new Context(locale);
    thymeleafContext.setVariables(templateModel);
    String htmlBody = thymeleafTemplateEngine.process(CANDIDATE_OFFER_TEMPLATE, thymeleafContext);

    byte[] attachment = proposalDocumentRenderer.render(resolved.getId(), locale);

    String fileName = "proposal-" + resolved.getId() + ".pdf";

    try {
      this.sendWithAttach(
          emailSender,
          resolved.getCandidate().getEmail(),
          subject,
          htmlBody,
          BANNER_CONTENT_ID,
          bannerResource(),
          BANNER_CONTENT_TYPE,
          fileName,
          new ByteArrayResource(attachment));
    } catch (MessagingException e) {
      throw new RuntimeException("Error sending email: " + e);
    }
  }
}
