package org.propflow.back.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.propflow.back.model.Proposal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
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
  @Autowired private MessageSource messageSource;

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
  public void sendProposalTo(String to, String subject, String body, String fileName) {

    Map<String, Object> templateModel = new HashMap<>();
    templateModel.put("body", body);
    Context thymeleafContext = new Context();
    thymeleafContext.setVariables(templateModel);
    String htmlBody = thymeleafTemplateEngine.process(OFFER_TEMPLATE, thymeleafContext);
    byte[] attachment = new byte[0];

    try {
      attachment = Files.readAllBytes(Paths.get(fileName));
    } catch (IOException e) {
      throw new RuntimeException("Error reading file to be attached: " + e);
    }

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
  public void notifyCandidateByEmail(Proposal proposal, String fileName, String language) {

    String subject = "PropFlow " + proposal.getDescription();
    Locale locale = localeFor(language);
    Map<String, Object> templateModel = new HashMap<>();
    templateModel.put("candidate", proposal.getCandidate().getName());
    templateModel.put("role", proposal.getRole());
    templateModel.put("office", proposal.getOffice().getName());

    Context thymeleafContext = new Context(locale);
    thymeleafContext.setVariables(templateModel);
    String htmlBody = thymeleafTemplateEngine.process(CANDIDATE_OFFER_TEMPLATE, thymeleafContext);

    byte[] attachment = new byte[0];

    try {
      attachment = Files.readAllBytes(Paths.get(fileName));
    } catch (IOException e) {
      throw new RuntimeException("Error reading file to be attached: " + e);
    }

    try {
      this.sendWithAttach(
          emailSender,
          proposal.getCandidate().getEmail(),
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

  // FILLS THE SHARED OFFER TEMPLATE WITH THE CALLER'S LANGUAGE:
  @Async
  public void sendTemplateToCandidate(Map<String, Object> data) {

    Map<String, Object> proposal = asMap(data.get("proposal"));
    Map<String, Object> candidate = asMap(proposal.get("candidate"));
    Map<String, Double> monthly = asNumberMap(data.get("monthly"));
    Map<String, Double> yearly = asNumberMap(data.get("yearly"));
    Locale locale = localeFor((String) data.get("language"));

    String subject = "PropFlow " + proposal.get("description");
    Map<String, Object> templateModel = new HashMap<>();
    templateModel.put("candidate", candidate.get("name"));
    templateModel.put("office", officeName(proposal.get("office")));
    templateModel.put("contract", contractType(proposal, locale));
    templateModel.put("date", stringValue(proposal.get("startDate")));
    templateModel.put("monthlyGross", stringValue(monthly.get("Gross Salary")));
    templateModel.put("yearlyGross", stringValue(yearly.get("Gross Salary")));
    templateModel.put("base", stringValue(monthly.get("Base Salary")));
    templateModel.put("iht", stringValue(monthly.get("IHT")));

    if (monthly.get("Food Allowance") != null) {
      templateModel.put("food", stringValue(monthly.get("Food Allowance")));
    }
    if (monthly.get("Car Allowance") != null) {
      templateModel.put("carMonthly", stringValue(monthly.get("Car Allowance")));
      templateModel.put("carYearly", stringValue(yearly.get("Car Allowance")));
    }
    if (monthly.get("Fuel Allowance") != null) {
      templateModel.put("fuelYearly", stringValue(yearly.get("Fuel Allowance")));
      templateModel.put("fuelMonthly", stringValue(monthly.get("Fuel Allowance")));
    }
    if (yearly.get("Flex") != null) {
      templateModel.put("yearlyFlex", stringValue(yearly.get("Flex")));
    }

    Context thymeleafContext = new Context(locale);
    thymeleafContext.setVariables(templateModel);
    String htmlBody = thymeleafTemplateEngine.process(OFFER_TEMPLATE, thymeleafContext);

    try {
      this.sendWithInline(
          emailSender,
          candidate.get("email").toString(),
          subject,
          htmlBody,
          BANNER_CONTENT_ID,
          bannerResource(),
          BANNER_CONTENT_TYPE);
    } catch (MessagingException e) {
      throw new RuntimeException("Error sending email: " + e);
    }
  }

  private Locale localeFor(String language) {
    if (language == null || language.isBlank()) {
      return Locale.ENGLISH;
    }
    return Locale.forLanguageTag(language);
  }

  private String contractType(Map<String, Object> proposal, Locale locale) {
    boolean indefinite = Boolean.TRUE.equals(proposal.get("indefinite"));
    String key = indefinite ? "contract.indefinite" : "contract.temporary";
    return messageSource.getMessage(key, null, locale);
  }

  private static String officeName(Object office) {
    if (office instanceof Map<?, ?> map) {
      Object name = map.get("name");
      return name != null ? name.toString() : "";
    }
    return office != null ? office.toString() : "";
  }

  private static String stringValue(Object value) {
    return value == null ? null : String.valueOf(value);
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> asMap(Object value) {
    return (Map<String, Object>) value;
  }

  private static Map<String, Double> asNumberMap(Object value) {
    Map<String, Double> map = new HashMap<>();
    if (!(value instanceof List<?> list)) {
      return map;
    }
    for (Object item : list) {
      if (item instanceof List<?> pair && pair.size() >= 2) {
        map.put(String.valueOf(pair.get(0)), Double.parseDouble(String.valueOf(pair.get(1))));
      }
    }
    return map;
  }
}
