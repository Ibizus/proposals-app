package org.propflow.back.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.propflow.back.config.SecurityConfig;
import org.propflow.back.exception.EntityNotFoundException;
import org.propflow.back.exception.NotCouplingIdException;
import org.propflow.back.model.Office;
import org.propflow.back.service.OfficeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OfficeController.class)
@Import(SecurityConfig.class)
class OfficeControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private OfficeService officeService;

  private static Office office(long id, String name, String code) {
    return new Office(id, name, code, new HashSet<>());
  }

  @Test
  void listsAllOffices() throws Exception {
    when(officeService.all())
        .thenReturn(List.of(office(1, "Lisbon", "LIS"), office(2, "Malaga", "MAL")));

    mockMvc
        .perform(get("/v1/api/offices"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].name").value("Lisbon"))
        .andExpect(jsonPath("$[1].code").value("MAL"));
  }

  @Test
  void filterParamRoutesToFilteredSearch() throws Exception {
    when(officeService.filter(Optional.of("mal")))
        .thenReturn(new PageImpl<>(List.of(office(2, "Malaga", "MAL"))));

    mockMvc
        .perform(get("/v1/api/offices").param("filter", "mal"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].name").value("Malaga"));
  }

  @Test
  void returnsOneOffice() throws Exception {
    when(officeService.one(1L)).thenReturn(office(1, "Lisbon", "LIS"));

    mockMvc
        .perform(get("/v1/api/offices/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("Lisbon"));
  }

  @Test
  void missingOfficeIsReportedByControllerAdvice() throws Exception {
    when(officeService.one(9L)).thenThrow(new EntityNotFoundException(9L, Office.class));

    // GeneralControllerAdvice maps every handled exception to 500 with the message as body.
    mockMvc
        .perform(get("/v1/api/offices/9"))
        .andExpect(status().isInternalServerError())
        .andExpect(content().string("Not found EntityOffice with id: 9"));
  }

  @Test
  void createsOfficeFromJsonBody() throws Exception {
    when(officeService.save(any(Office.class)))
        .thenAnswer(
            invocation -> {
              Office saved = invocation.getArgument(0);
              saved.setId(5);
              return saved;
            });

    mockMvc
        .perform(
            post("/v1/api/offices")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Porto\",\"code\":\"OPO\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(5))
        .andExpect(jsonPath("$.name").value("Porto"))
        .andExpect(jsonPath("$.code").value("OPO"));
  }

  @Test
  void replaceWithMismatchedIdIsRejected() throws Exception {
    when(officeService.replace(eq(1L), any(Office.class)))
        .thenThrow(new NotCouplingIdException(1L, 2L, Office.class));

    mockMvc
        .perform(
            put("/v1/api/offices/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":2,\"name\":\"Porto\",\"code\":\"OPO\"}"))
        .andExpect(status().isInternalServerError())
        .andExpect(content().string("Not coupling id1 = 1 with id2 = 2 for entity Office"));
  }

  @Test
  void deleteReturnsNoContent() throws Exception {
    mockMvc.perform(delete("/v1/api/offices/1")).andExpect(status().isNoContent());

    verify(officeService).delete(1L);
  }

  @Test
  void deleteOfMissingOfficeIsReported() throws Exception {
    doThrow(new EntityNotFoundException(3L, Office.class)).when(officeService).delete(3L);

    mockMvc.perform(delete("/v1/api/offices/3")).andExpect(status().isInternalServerError());
  }
}
