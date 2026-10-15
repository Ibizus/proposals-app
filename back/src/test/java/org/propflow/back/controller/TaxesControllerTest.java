package org.propflow.back.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.propflow.back.config.SecurityConfig;
import org.propflow.back.model.Taxes;
import org.propflow.back.service.TaxesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TaxesController.class)
@Import(SecurityConfig.class)
class TaxesControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private TaxesService taxesService;

  private static Taxes bracket(long id, double salaryMax, int maritalStatus) {
    Taxes taxes = new Taxes();
    taxes.setId(id);
    taxes.setSalaryMax(salaryMax);
    taxes.setMaritalStatus(maritalStatus);
    return taxes;
  }

  @Test
  void withoutParamsListsAllBrackets() throws Exception {
    when(taxesService.all()).thenReturn(List.of(bracket(1, 1000, 0), bracket(2, 1000, 1)));

    mockMvc.perform(get("/v1/api/taxes"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2));
  }

  @Test
  void salaryParamFindsBracket() throws Exception {
    when(taxesService.findBySalary(950d)).thenReturn(bracket(1, 1000, 0));

    mockMvc.perform(get("/v1/api/taxes").param("salary", "950"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.salaryMax").value(1000.0));
  }

  @Test
  void maritalStatusParamListsBrackets() throws Exception {
    when(taxesService.findByMaritalStatus(1)).thenReturn(List.of(bracket(2, 1000, 1), bracket(3, 2000, 1)));

    mockMvc.perform(get("/v1/api/taxes").param("maritalStatus", "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[1].salaryMax").value(2000.0));
  }

  @Test
  void salaryAndMaritalStatusFindBracket() throws Exception {
    when(taxesService.findBySalaryAndMaritalStatus(1500d, 1)).thenReturn(bracket(3, 2000, 1));

    mockMvc.perform(get("/v1/api/taxes").param("salary", "1500").param("maritalStatus", "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(3))
        .andExpect(jsonPath("$.maritalStatus").value(1));
  }
}
