package org.propflow.back.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.propflow.back.config.SecurityConfig;
import org.propflow.back.dto.TaxConfigurationDTO;
import org.propflow.back.mapper.TaxConfigurationMapper;
import org.propflow.back.model.TaxConfiguration;
import org.propflow.back.service.TaxConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TaxConfigurationController.class)
@Import(SecurityConfig.class)
class TaxConfigurationControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private TaxConfigurationService taxConfigurationService;
  @MockBean private TaxConfigurationMapper taxConfigurationMapper;

  @Test
  void listsAllConfigurations() throws Exception {
    TaxConfiguration config = new TaxConfiguration();
    config.setId(1L);
    config.setCountryCode("es");
    config.setLabel("Spain");
    config.setYearlyPayments(12);
    config.setActive(true);
    when(taxConfigurationService.all()).thenReturn(List.of(config));
    when(taxConfigurationMapper.toDTO(config))
        .thenReturn(new TaxConfigurationDTO(1L, "es", "Spain", 12, true, List.of()));

    mockMvc
        .perform(get("/v1/api/tax-configurations"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].countryCode").value("es"));
  }

  @Test
  void returnsActiveConfiguration() throws Exception {
    TaxConfiguration config = new TaxConfiguration();
    config.setId(2L);
    config.setCountryCode("pt");
    config.setLabel("Portugal");
    config.setYearlyPayments(14);
    config.setActive(false);
    when(taxConfigurationService.findActive()).thenReturn(config);
    when(taxConfigurationMapper.toDTO(config))
        .thenReturn(new TaxConfigurationDTO(2L, "pt", "Portugal", 14, false, List.of()));

    mockMvc
        .perform(get("/v1/api/tax-configurations/active"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.countryCode").value("pt"))
        .andExpect(jsonPath("$.yearlyPayments").value(14));
  }

  @Test
  void createsConfiguration() throws Exception {
    TaxConfiguration entity = new TaxConfiguration();
    TaxConfigurationDTO dto =
        new TaxConfigurationDTO(null, "es", "Spain (illustrative)", 12, true, List.of());
    when(taxConfigurationMapper.toEntity(dto)).thenReturn(entity);
    when(taxConfigurationService.save(entity)).thenReturn(entity);
    when(taxConfigurationMapper.toDTO(entity))
        .thenReturn(new TaxConfigurationDTO(3L, "es", "Spain (illustrative)", 12, true, List.of()));

    mockMvc
        .perform(
            post("/v1/api/tax-configurations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"countryCode\":\"es\",\"label\":\"Spain (illustrative)\",\"yearlyPayments\":12,\"active\":true,\"taxes\":[]}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(3));
  }

  @Test
  void activatesConfiguration() throws Exception {
    TaxConfiguration config = new TaxConfiguration();
    config.setId(2L);
    when(taxConfigurationService.activate(2L)).thenReturn(config);
    when(taxConfigurationMapper.toDTO(config))
        .thenReturn(new TaxConfigurationDTO(2L, "pt", "Portugal", 14, true, List.of()));

    mockMvc
        .perform(post("/v1/api/tax-configurations/2/activate"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.active").value(true));
  }

  @Test
  void deletesConfiguration() throws Exception {
    mockMvc.perform(delete("/v1/api/tax-configurations/2")).andExpect(status().isNoContent());
  }
}
