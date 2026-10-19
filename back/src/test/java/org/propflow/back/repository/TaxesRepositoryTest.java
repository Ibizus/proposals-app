package org.propflow.back.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.propflow.back.model.Taxes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
// Use the H2 URL from src/test/resources (MySQL mode, VALUE allowed as a column name).
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TaxesRepositoryTest {

  @Autowired private TaxesRepository taxesRepository;

  private static Taxes bracket(double salaryMax, int maritalStatus, double irsZero) {
    return new Taxes(
        0,
        salaryMax,
        11d,
        23.75,
        LocalDate.of(2024, 1, 1),
        LocalDate.of(2024, 12, 31),
        maritalStatus,
        irsZero,
        0d,
        0d,
        0d,
        0d,
        0d);
  }

  @BeforeEach
  void setUp() {
    taxesRepository.save(bracket(2000, 0, 20));
    taxesRepository.save(bracket(1000, 0, 10));
    taxesRepository.save(bracket(1500, 1, 15));
    taxesRepository.save(bracket(3000, 1, 30));
  }

  @Test
  void findsSmallestBracketCoveringSalaryForMaritalStatus() {
    assertThat(
            taxesRepository
                .findFirstBySalaryMaxGreaterThanEqualAndMaritalStatusOrderBySalaryMaxAsc(1200, 0)
                .getSalaryMax())
        .isEqualTo(2000d);
    assertThat(
            taxesRepository
                .findFirstBySalaryMaxGreaterThanEqualAndMaritalStatusOrderBySalaryMaxAsc(1000, 0)
                .getSalaryMax())
        .isEqualTo(1000d);
    assertThat(
            taxesRepository
                .findFirstBySalaryMaxGreaterThanEqualAndMaritalStatusOrderBySalaryMaxAsc(1200, 1)
                .getSalaryMax())
        .isEqualTo(1500d);
  }

  @Test
  void returnsNullWhenSalaryExceedsEveryBracket() {
    assertThat(
            taxesRepository.findFirstBySalaryMaxGreaterThanEqualAndMaritalStatusOrderBySalaryMaxAsc(
                5000, 0))
        .isNull();
  }

  @Test
  void findsSmallestBracketCoveringSalaryAcrossStatuses() {
    assertThat(
            taxesRepository
                .findFirstBySalaryMaxGreaterThanEqualOrderBySalaryMaxAsc(1100)
                .getSalaryMax())
        .isEqualTo(1500d);
  }

  @Test
  void listsBracketsForMaritalStatusInAscendingOrder() {
    assertThat(taxesRepository.findByMaritalStatusOrderBySalaryMaxAsc(0))
        .extracting(Taxes::getSalaryMax)
        .containsExactly(1000d, 2000d);
  }
}
