package org.propflow.back.controller;

import static org.hamcrest.Matchers.containsInAnyOrder;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.propflow.back.config.SecurityConfig;
import org.propflow.back.dto.PositionDTO;
import org.propflow.back.exception.EntityNotFoundException;
import org.propflow.back.exception.NotCouplingIdException;
import org.propflow.back.mapper.PositionMapper;
import org.propflow.back.model.Position;
import org.propflow.back.service.PositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PositionController.class)
@Import(SecurityConfig.class)
class PositionControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private PositionService positionService;
  @MockBean private PositionMapper positionMapper;

  private static final LocalDateTime T0 = LocalDateTime.of(2024, 4, 1, 9, 0);

  private static PositionDTO dto(long id, String title) {
    return new PositionDTO(
        id, title, "description", "NJ-" + id, "more", Set.of(1L, 2L), T0, null, T0, null);
  }

  private static Map<String, Object> page(List<PositionDTO> positions) {
    return Map.of(
        "positions",
        positions,
        "currentPage",
        0,
        "totalItems",
        (long) positions.size(),
        "totalPages",
        1);
  }

  @Test
  void listsAllPositions() throws Exception {
    when(positionService.all()).thenReturn(List.of(dto(1, "Backend"), dto(2, "Frontend")));

    mockMvc
        .perform(get("/v1/api/positions"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].title").value("Backend"))
        .andExpect(jsonPath("$[1].title").value("Frontend"))
        .andExpect(jsonPath("$[0].candidates.length()").value(2));
  }

  @Test
  void pageAndSizeRouteToPaginatedList() throws Exception {
    when(positionService.all(2, 5)).thenReturn(page(List.of(dto(11, "DevOps"))));

    mockMvc
        .perform(get("/v1/api/positions").param("page", "2").param("size", "5"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.positions[0].title").value("DevOps"))
        .andExpect(jsonPath("$.totalItems").value(1));
  }

  @Test
  void returnsOnePosition() throws Exception {
    when(positionService.one(1L))
        .thenReturn(new Position(1, "Backend", "desc", "NJ-1", "more", null));
    when(positionMapper.toDTO(any(Position.class))).thenReturn(dto(1, "Backend"));

    mockMvc
        .perform(get("/v1/api/positions/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.title").value("Backend"));
  }

  @Test
  void missingPositionIsReportedByControllerAdvice() throws Exception {
    when(positionService.one(9L)).thenThrow(new EntityNotFoundException(9L, Position.class));

    mockMvc
        .perform(get("/v1/api/positions/9"))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.type").value("urn:propflow:problem:entity-not-found"))
        .andExpect(jsonPath("$.title").value("Entity not found"))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.detail").value("Not found EntityPosition with id: 9"));
  }

  @Test
  void blankPositionFieldsAreRejectedWithFieldErrors() throws Exception {
    mockMvc
        .perform(
            post("/v1/api/positions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"\",\"description\":\"\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.type").value("urn:propflow:problem:validation-failed"))
        .andExpect(jsonPath("$.title").value("Validation failed"))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.errors.length()").value(2))
        .andExpect(jsonPath("$.errors[*].field").value(containsInAnyOrder("title", "description")));
  }

  @Test
  void createsPositionFromJsonBody() throws Exception {
    when(positionService.save(any(Position.class)))
        .thenAnswer(
            invocation -> {
              Position saved = invocation.getArgument(0);
              saved.setId(5);
              return saved;
            });

    mockMvc
        .perform(
            post("/v1/api/positions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"title\":\"Backend\",\"description\":\"Build services\",\"njoyn\":\"NJ-9\",\"knowMore\":\"more\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(5))
        .andExpect(jsonPath("$.title").value("Backend"))
        .andExpect(jsonPath("$.description").value("Build services"));
  }

  @Test
  void replaceWithMismatchedIdIsRejected() throws Exception {
    when(positionService.replace(eq(1L), any(Position.class)))
        .thenThrow(new NotCouplingIdException(1L, 2L, Position.class));

    mockMvc
        .perform(
            put("/v1/api/positions/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":2,\"title\":\"Backend\",\"description\":\"desc\"}"))
        .andExpect(status().isConflict())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.type").value("urn:propflow:problem:coupling-id-mismatch"))
        .andExpect(jsonPath("$.title").value("Identifier mismatch"))
        .andExpect(jsonPath("$.status").value(409))
        .andExpect(
            jsonPath("$.detail").value("Not coupling id1 = 1 with id2 = 2 for entity Position"));
  }

  @Test
  void deleteReturnsNoContent() throws Exception {
    mockMvc.perform(delete("/v1/api/positions/1")).andExpect(status().isNoContent());

    verify(positionService).delete(1L);
  }

  @Test
  void deleteOfMissingPositionIsReported() throws Exception {
    doThrow(new EntityNotFoundException(3L, Position.class)).when(positionService).delete(3L);

    mockMvc.perform(delete("/v1/api/positions/3")).andExpect(status().isNotFound());
  }
}
