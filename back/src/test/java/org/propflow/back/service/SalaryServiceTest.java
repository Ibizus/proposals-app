package org.propflow.back.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.propflow.back.model.Candidate;
import org.propflow.back.model.Extra;
import org.propflow.back.model.Proposal;
import org.propflow.back.model.Salary;
import org.propflow.back.model.Taxes;
import org.propflow.back.repository.SalaryRepository;

@ExtendWith(MockitoExtension.class)
class SalaryServiceTest {

  private static final Offset<Double> CENT = within(0.001);

  @Mock private SalaryRepository salaryRepository;
  @Mock private ParameterService parameterService;
  @Mock private TaxesService taxesService;

  @InjectMocks private SalaryService salaryService;

  private Taxes bracket;

  @BeforeEach
  void setUp() {
    bracket = new Taxes();
    bracket.setSalaryMax(1051d);
    bracket.setSocialSecurityTax(11d);
    bracket.setMaritalStatus(0);
    bracket.setZero(12d);
    bracket.setOne(10d);
    when(taxesService.findBySalaryAndMaritalStatus(1000d, 0)).thenReturn(bracket);
    when(parameterService.getCalculationsParameters())
        .thenReturn(Map.of("iht", 20.0, "spp", 3.0, "scr", 0.9235));
  }

  private static Proposal proposal(double base, int dependants, Extra... extras) {
    Candidate candidate = new Candidate();
    candidate.setMaritalStatus(0);
    candidate.setDependentsNumber(dependants);
    Salary salary = new Salary(0, base, null, new HashSet<>(List.of(extras)));
    Proposal proposal = new Proposal();
    proposal.setSalary(salary);
    proposal.setCandidate(candidate);
    return proposal;
  }

  private static Extra extra(
      String name,
      double amount,
      boolean percentage,
      boolean ssTaxable,
      boolean irsTaxable,
      int multiplyYearly,
      int multiplyMonthly) {
    Extra extra = new Extra();
    extra.setName(name);
    extra.setAmount(amount);
    extra.setIsPercentage(percentage);
    extra.setIsSSTaxable(ssTaxable);
    extra.setIsIRSTaxable(irsTaxable);
    extra.setAddToSalary(false);
    extra.setMultiplyYearly(multiplyYearly);
    extra.setMultiplyMonthly(multiplyMonthly);
    return extra;
  }

  @Test
  void computesMonthlyAndYearlyBaseComponents() {
    when(taxesService.getIrsByNumberOfDependants(bracket, 1)).thenCallRealMethod();

    Map<String, Map<String, Double>> result =
        salaryService.salaryCalculations(proposal(1000, 1), null, null);
    Map<String, Double> monthly = result.get("monthly");
    Map<String, Double> yearly = result.get("yearly");

    assertThat(monthly.get("Gross Salary")).isCloseTo(1000, CENT);
    assertThat(yearly.get("Gross Salary")).isCloseTo(14000, CENT);
    // 1000 * (1 - 20%) * 0.9235
    assertThat(monthly.get("Base Salary")).isCloseTo(738.8, CENT);
    assertThat(yearly.get("Base Salary")).isCloseTo(10343.2, CENT);
    assertThat(monthly.get("IHT")).isCloseTo(200, CENT);
    assertThat(yearly.get("IHT")).isCloseTo(2800, CENT);
    assertThat(monthly.get("Share Purchase Plan")).isCloseTo(30, CENT);
    assertThat(yearly.get("Share Purchase Plan")).isCloseTo(420, CENT);
    // 1000 - (11% SS + 10% IRS for one dependant)
    assertThat(monthly.get("Estimated Net Salary")).isCloseTo(790, CENT);
    assertThat(yearly.get("Estimated Net Salary")).isCloseTo(11060, CENT);
  }

  @Test
  void forcedIrsRateOverridesBracket() {
    Map<String, Map<String, Double>> result =
        salaryService.salaryCalculations(proposal(1000, 1), 5.0, null);

    // 1000 - (11% SS + 5% forced IRS)
    assertThat(result.get("monthly").get("Estimated Net Salary")).isCloseTo(840, CENT);
  }

  @Test
  void monthlyExtrasAreTaxedPerFlagsAndMultiplied() {
    when(taxesService.getIrsByNumberOfDependants(bracket, 0)).thenCallRealMethod();
    // 15/day for 21 days, 11 months a year, IRS-taxable only
    Extra food = extra("Food Allowance", 15, false, false, true, 11, 21);

    Map<String, Map<String, Double>> result =
        salaryService.salaryCalculations(proposal(1000, 0, food), null, null);

    // 315 - 12% IRS
    assertThat(result.get("monthly").get("Food Allowance")).isCloseTo(277.2, CENT);
    assertThat(result.get("yearly").get("Food Allowance")).isCloseTo(3049.2, CENT);
  }

  @Test
  void yearlyOnlyPercentageExtrasAreMissingFromMonthly() {
    when(taxesService.getIrsByNumberOfDependants(bracket, 0)).thenCallRealMethod();
    // 4% of salary, paid 12 times a year, SS-taxable only, not monthly (-1)
    Extra flex = extra("Flex", 4, true, true, false, 12, -1);

    Map<String, Map<String, Double>> result =
        salaryService.salaryCalculations(proposal(1000, 0, flex), null, null);

    assertThat(result.get("monthly")).doesNotContainKey("Flex");
    // 40 * 12 = 480, minus 11% SS
    assertThat(result.get("yearly").get("Flex")).isCloseTo(427.2, CENT);
  }

  @Test
  void missingParametersDefaultToZero() {
    when(parameterService.getCalculationsParameters()).thenReturn(Map.of());
    when(taxesService.getIrsByNumberOfDependants(bracket, 0)).thenCallRealMethod();

    Map<String, Double> monthly =
        salaryService.salaryCalculations(proposal(1000, 0), null, null).get("monthly");

    assertThat(monthly.get("Base Salary")).isCloseTo(0, CENT);
    assertThat(monthly.get("IHT")).isCloseTo(0, CENT);
    assertThat(monthly.get("Share Purchase Plan")).isCloseTo(0, CENT);
  }
}
