package org.propflow.back.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
  public void sendMail(
      @Valid @RequestBody Proposal proposal,
      @RequestParam(required = false, defaultValue = "en") String language) {

    log.info("Proposal received in backend to be sent");
    this.mailSenderService.notifyCandidateByEmail(proposal, language);
    log.info("Message processing in backend!");
  }

  @Operation(summary = "Send a proposal email to an address")
  @PostMapping(value = {"to", "to/"})
  public void sendMail(
      @RequestParam String to,
      @RequestParam String subject,
      @RequestBody String body,
      @RequestParam(required = false) Long proposalId,
      @RequestParam(required = false, defaultValue = "en") String language) {

    this.mailSenderService.sendProposalTo(to, subject, body, proposalId, language);
    log.info("Message processing in backend!");
  }
}
