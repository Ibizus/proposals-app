package org.propflow.back.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.propflow.back.config.SecurityConfig;
import org.propflow.back.model.Candidate;
import org.propflow.back.service.CandidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CandidateController.class)
@Import(SecurityConfig.class)
class CandidateControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private CandidateService candidateService;

  private static Candidate candidate(long id, String name) {
    Candidate candidate = new Candidate();
    candidate.setId(id);
    candidate.setName(name);
    return candidate;
  }

  private static Map<String, Object> page(List<Candidate> candidates) {
    return Map.of("candidates", candidates, "currentPage", 0, "totalItems", (long) candidates.size(),
        "totalPages", 1);
  }

  @Test
  void withoutParamsListsEveryCandidate() throws Exception {
    when(candidateService.all()).thenReturn(List.of(candidate(1, "Ann"), candidate(2, "Bob")));

    mockMvc.perform(get("/v1/api/candidates"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[1].name").value("Bob"));
  }

  @Test
  void pageAndSizeRouteToPaginatedList() throws Exception {
    when(candidateService.all(2, 5)).thenReturn(page(List.of(candidate(11, "Kim"))));

    mockMvc.perform(get("/v1/api/candidates").param("page", "2").param("size", "5"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.candidates[0].name").value("Kim"))
        .andExpect(jsonPath("$.totalItems").value(1));
  }

  @Test
  void pageSizeAndFilterRouteToFilteredPage() throws Exception {
    when(candidateService.findByFilter(1, 4, "ann")).thenReturn(page(List.of(candidate(1, "Ann"))));

    mockMvc.perform(get("/v1/api/candidates").param("page", "1").param("size", "4").param("filter", "ann"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.candidates[0].name").value("Ann"));
  }

  @Test
  void filterAloneUsesFirstPageOfTen() throws Exception {
    when(candidateService.findByFilter(0, 10, "ann")).thenReturn(page(List.of(candidate(1, "Ann"))));

    mockMvc.perform(get("/v1/api/candidates").param("filter", "ann"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.candidates[0].name").value("Ann"));

    verify(candidateService).findByFilter(0, 10, "ann");
  }

  @Test
  void createsCandidate() throws Exception {
    when(candidateService.save(any(Candidate.class))).thenAnswer(invocation -> {
      Candidate saved = invocation.getArgument(0);
      saved.setId(3);
      return saved;
    });

    mockMvc.perform(post("/v1/api/candidates")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"name\":\"Eve\",\"idNumber\":\"ID-9\",\"maritalStatus\":1,\"dependentsNumber\":2}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(3))
        .andExpect(jsonPath("$.name").value("Eve"))
        .andExpect(jsonPath("$.dependentsNumber").value(2));
  }
}
