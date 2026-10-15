package org.propflow.back.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.propflow.back.exception.EntityNotFoundException;
import org.propflow.back.exception.NotCouplingIdException;
import org.propflow.back.model.Taxes;
import org.propflow.back.repository.TaxesRepository;

@ExtendWith(MockitoExtension.class)
class TaxesServiceTest {

  @Mock
  private TaxesRepository taxesRepository;

  @InjectMocks
  private TaxesService taxesService;

  private static Taxes bracket() {
    Taxes taxes = new Taxes();
    taxes.setId(1);
    taxes.setSalaryMax(1000d);
    taxes.setSocialSecurityTax(11d);
    taxes.setMaritalStatus(0);
    taxes.setZero(10d);
    taxes.setOne(9d);
    taxes.setTwo(8d);
    taxes.setThree(7d);
    taxes.setFour(6d);
    taxes.setFive(5d);
    return taxes;
  }

  @ParameterizedTest(name = "{0} dependants -> {1}% IRS")
  @CsvSource({ "-1, 10", "0, 10", "1, 9", "2, 8", "3, 7", "4, 6", "5, 5", "9, 5" })
  void irsRateDependsOnNumberOfDependants(int dependants, double expectedIrs) {
    assertThat(taxesService.getIrsByNumberOfDependants(bracket(), dependants)).isEqualTo(expectedIrs);
  }

  @Test
  void findBySalaryAndMaritalStatusDelegatesToBracketQuery() {
    Taxes taxes = bracket();
    when(taxesRepository.findFirstBySalaryMaxGreaterThanEqualAndMaritalStatusOrderBySalaryMaxAsc(950d, 0))
        .thenReturn(taxes);

    assertThat(taxesService.findBySalaryAndMaritalStatus(950d, 0)).isSameAs(taxes);
  }

  @Test
  void oneThrowsWhenMissing() {
    when(taxesRepository.findById(3L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> taxesService.one(3L)).isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  void replaceRejectsMismatchedIds() {
    when(taxesRepository.findById(2L)).thenReturn(Optional.of(bracket()));

    assertThatThrownBy(() -> taxesService.replace(2L, bracket()))
        .isInstanceOf(NotCouplingIdException.class);
    verify(taxesRepository, never()).save(any());
  }
}
