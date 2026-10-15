package org.propflow.back.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.propflow.back.model.Parameter;
import org.propflow.back.repository.ParameterRepository;

@ExtendWith(MockitoExtension.class)
class ParameterServiceTest {

  @Mock
  private ParameterRepository parameterRepository;

  @InjectMocks
  private ParameterService parameterService;

  @Test
  void calculationParametersKeepOnlyIhtSppAndScr() {
    when(parameterRepository.findAll()).thenReturn(List.of(
        new Parameter(1, "iht", 20.0, null, null),
        new Parameter(2, "spp", 3.0, null, null),
        new Parameter(3, "scr", 0.9235, null, null),
        new Parameter(4, "unrelated", 99.0, null, null)));

    assertThat(parameterService.getCalculationsParameters())
        .containsOnlyKeys("iht", "spp", "scr")
        .containsEntry("iht", 20.0)
        .containsEntry("spp", 3.0)
        .containsEntry("scr", 0.9235);
  }
}
