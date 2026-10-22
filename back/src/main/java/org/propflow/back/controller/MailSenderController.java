package org.propflow.back.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.propflow.back.model.Proposal;
import org.propflow.back.service.MailSenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
@RestController
@RequestMapping("/v1/api/send/")
@Tag(name = "mail", description = "Email sending")
public class MailSenderController {

  @Autowired private MailSenderService mailSenderService;

  @Operation(summary = "Send a candidate proposal email")
  @PostMapping(value = {"candidate", "candidate/"})
  public void sendMail(@Valid @RequestBody Proposal proposal, @RequestParam String filename) {

    log.info("Proposal received in backend to be sent");
    this.mailSenderService.notifyCandidateByEmail(proposal, filename);
    log.info("Message processing in backend!");
  }

  @Operation(summary = "Send a proposal email to an address")
  @PostMapping(
      value = {"to", "to/"},
      params = {"to", "subject", "filename"})
  public void sendMail(
      @RequestParam String to,
      @RequestParam String subject,
      @RequestBody String body,
      @RequestParam String filename) {

    this.mailSenderService.sendProposalTo(to, subject, body, filename);
    log.info("Message processing in backend!");
  }

  @Operation(summary = "Send a template-filled email")
  @PostMapping(value = {"template", "template/"})
  public void sendMail(@RequestBody Map<String, Object> data) {

    log.info("Data received in backend to fill html template");
    this.mailSenderService.sendTemplateToCandidate(data);
  }
}
