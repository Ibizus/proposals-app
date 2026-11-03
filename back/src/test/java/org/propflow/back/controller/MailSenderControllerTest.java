package org.propflow.back.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.mail.MessagingException;
import org.junit.jupiter.api.Test;
import org.propflow.back.config.SecurityConfig;
import org.propflow.back.model.Proposal;
import org.propflow.back.service.MailSenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MailSenderController.class)
@Import(SecurityConfig.class)
class MailSenderControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private MailSenderService mailSenderService;

  private static final String CANDIDATE_URL = "/v1/api/send/candidate/";
  private static final String TO_URL = "/v1/api/send/to/";
  private static final String VALID_PROPOSAL_JSON =
      "{"
          + "\"id\":1,"
          + "\"description\":\"Backend Engineer\","
          + "\"role\":\"Backend Engineer\","
          + "\"office\":{\"id\":1,\"name\":\"Lisbon\",\"code\":\"LIS\"},"
          + "\"salary\":{\"id\":1,\"base\":1000.0,\"extras\":[]},"
          + "\"candidate\":{\"id\":1,\"name\":\"Ada\",\"surname\":\"Lovelace\",\"idNumber\":\"ID-1\",\"phone\":\"600000000\",\"email\":\"ada@example.com\",\"maritalStatus\":0,\"dependentsNumber\":0}"
          + "}";

  @Test
  void sendCandidateNotificationSucceeds() throws Exception {
    mockMvc
        .perform(
            post(CANDIDATE_URL)
                .param("language", "en")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_PROPOSAL_JSON))
        .andExpect(status().isOk());

    verify(mailSenderService).notifyCandidateByEmail(any(Proposal.class), anyString());
  }

  @Test
  void sendCandidateNotificationFailureReachesCaller() throws Exception {
    doThrow(new RuntimeException("Error sending email: mail server unreachable"))
        .when(mailSenderService)
        .notifyCandidateByEmail(any(Proposal.class), anyString());

    mockMvc
        .perform(
            post(CANDIDATE_URL)
                .param("language", "en")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_PROPOSAL_JSON))
        .andExpect(status().isInternalServerError())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.type").value("urn:propflow:problem:internal-error"))
        .andExpect(jsonPath("$.status").value(500));
  }

  @Test
  void sendProposalToSucceeds() throws Exception {
    mockMvc
        .perform(
            post(TO_URL)
                .param("to", "recruiter@example.com")
                .param("subject", "Offer")
                .param("proposalId", "1")
                .param("language", "en")
                .contentType(MediaType.TEXT_PLAIN)
                .content("Please review the attached proposal."))
        .andExpect(status().isOk());

    verify(mailSenderService)
        .sendProposalTo(anyString(), anyString(), anyString(), anyLong(), anyString());
  }

  @Test
  void sendProposalToFailureReachesCaller() throws Exception {
    doThrow(new RuntimeException("Error sending email: " + new MessagingException(" refused")))
        .when(mailSenderService)
        .sendProposalTo(anyString(), anyString(), anyString(), anyLong(), anyString());

    mockMvc
        .perform(
            post(TO_URL)
                .param("to", "recruiter@example.com")
                .param("subject", "Offer")
                .param("proposalId", "1")
                .param("language", "en")
                .contentType(MediaType.TEXT_PLAIN)
                .content("Please review the attached proposal."))
        .andExpect(status().isInternalServerError())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.type").value("urn:propflow:problem:internal-error"))
        .andExpect(jsonPath("$.status").value(500));
  }
}
