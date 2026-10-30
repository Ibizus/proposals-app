package org.propflow.back.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import org.propflow.back.model.Tax;
import org.propflow.back.model.TaxBracket;
import org.propflow.back.model.TaxConfiguration;
import org.propflow.back.model.TaxType;
import org.propflow.back.repository.SalaryRepository;
import org.propflow.back.repository.TaxConfigurationRepository;

@ExtendWith(MockitoExtension.class)
class SalaryServiceTest {

  private static final Offset<Double> CENT = within(0.001);

  @Mock private SalaryRepository salaryRepository;
  @Mock private ParameterService parameterService;
  @Mock private TaxConfigurationRepository taxConfigurationRepository;

  @InjectMocks private SalaryService salaryService;

  private TaxConfiguration configuration;
  private Tax socialSecurity;
  private Tax incomeTax;

  @BeforeEach
  void setUp() {
    configuration = new TaxConfiguration();
    configuration.setId(1);
    configuration.setCountryCode("pt");
    configuration.setYearlyPayments(14);

    socialSecurity = new Tax();
    socialSecurity.setId(10);
    socialSecurity.setName("Social Security");
    socialSecurity.setType(TaxType.FIXED);
    socialSecurity.setFixedRate(11.0);
    socialSecurity.setConfiguration(configuration);

    incomeTax = new Tax();
    incomeTax.setId(20);
    incomeTax.setName("IRS");
    incomeTax.setType(TaxType.BRACKETED);
    incomeTax.setConfiguration(configuration);
    incomeTax.setBrackets(
        List.of(
            bracket(incomeTax, 500, 5.0),
            bracket(incomeTax, 1000, 10.0),
            bracket(incomeTax, 9999999, 20.0)));

    configuration.setTaxes(List.of(socialSecurity, incomeTax));

    when(taxConfigurationRepository.findByActiveTrue()).thenReturn(Optional.of(configuration));
    when(parameterService.getCalculationsParameters())
        .thenReturn(Map.of("iht", 20.0, "spp", 3.0, "scr", 0.9235));
  }

  private TaxBracket bracket(Tax tax, double threshold, double rate) {
    TaxBracket bracket = new TaxBracket();
    bracket.setThreshold(threshold);
    bracket.setRate(rate);
    bracket.setTax(tax);
    return bracket;
  }

  private static Proposal proposal(double base, Extra... extras) {
    Candidate candidate = new Candidate();
    candidate.setMaritalStatus(0);
    candidate.setDependentsNumber(1);
    Salary salary = new Salary(0, base, null, null, new HashSet<>(List.of(extras)));
    Proposal proposal = new Proposal();
    proposal.setSalary(salary);
    proposal.setCandidate(candidate);
    return proposal;
  }

  private static Extra extra(
      String name,
      double amount,
      boolean percentage,
      boolean exempt,
      List<Long> taxIds,
      int multiplyYearly,
      int multiplyMonthly) {
    Extra extra = new Extra();
    extra.setName(name);
    extra.setAmount(amount);
    extra.setIsPercentage(percentage);
    extra.setExempt(exempt);
    extra.setTaxIds(new HashSet<>(taxIds));
    extra.setAddToSalary(false);
    extra.setMultiplyYearly(multiplyYearly);
    extra.setMultiplyMonthly(multiplyMonthly);
    return extra;
  }

  @Test
  void computesMonthlyAndYearlyBaseComponents() {
    Map<String, Map<String, Double>> result =
        salaryService.salaryCalculations(proposal(1000), null, null);
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
    // 1000 - (11% SS + 75 IRS: 500*5% + 500*10%)
    assertThat(monthly.get("Estimated Net Salary")).isCloseTo(815, CENT);
    assertThat(yearly.get("Estimated Net Salary")).isCloseTo(11410, CENT);
  }

  @Test
  void forcedIncomeTaxRateOverridesBracket() {
    Map<String, Map<String, Double>> result =
        salaryService.salaryCalculations(proposal(1000), 5.0, null);

    // 1000 - (11% SS + 5% forced IRS)
    assertThat(result.get("monthly").get("Estimated Net Salary")).isCloseTo(840, CENT);
  }

  @Test
  void fixedRateTaxIsAppliedToBase() {
    Map<String, Double> monthly =
        salaryService.salaryCalculations(proposal(1000), null, null).get("monthly");
    // SS fixed 11% of 1000 + IRS 75
    assertThat(monthly.get("Estimated Net Salary")).isCloseTo(815, CENT);
  }

  @Test
  void progressiveBracketTaxesOnlyThePartInsideEachBracket() {
    // Base 1500: first 500 at 5% = 25, next 500 at 10% = 50, remaining 500 at 20% = 100
    Map<String, Double> monthly =
        salaryService.salaryCalculations(proposal(1500), null, null).get("monthly");
    // SS = 165, IRS = 175 -> net = 1500 - 340 = 1160
    assertThat(monthly.get("Estimated Net Salary")).isCloseTo(1160, CENT);
  }

  @Test
  void exemptExtraPaysNoTax() {
    Extra food = extra("Food Allowance", 100, false, true, List.of(), 11, 1);

    Map<String, Map<String, Double>> result =
        salaryService.salaryCalculations(proposal(1000, food), null, null);

    assertThat(result.get("monthly").get("Food Allowance")).isCloseTo(100, CENT);
    assertThat(result.get("yearly").get("Food Allowance")).isCloseTo(1100, CENT);
  }

  @Test
  void extraWithSubsetOfTaxesPaysOnlyThoseTaxes() {
    // Taxable by SS only
    Extra allowance = extra("Car Allowance", 100, false, false, List.of(10L), 12, 1);

    Map<String, Map<String, Double>> result =
        salaryService.salaryCalculations(proposal(1000, allowance), null, null);

    // 100 - 11% SS = 89
    assertThat(result.get("monthly").get("Car Allowance")).isCloseTo(89, CENT);
    assertThat(result.get("yearly").get("Car Allowance")).isCloseTo(1068, CENT);
  }

  @Test
  void yearlyOnlyExtrasAreMissingFromMonthly() {
    Extra flex = extra("Flex", 4, true, false, List.of(10L), 12, -1);

    Map<String, Map<String, Double>> result =
        salaryService.salaryCalculations(proposal(1000, flex), null, null);

    assertThat(result.get("monthly")).doesNotContainKey("Flex");
    // 40 * 12 = 480, minus 11% SS = 427.2
    assertThat(result.get("yearly").get("Flex")).isCloseTo(427.2, CENT);
  }

  @Test
  void nonDefaultPaymentsCountIsUsed() {
    configuration.setYearlyPayments(12);

    Map<String, Map<String, Double>> result =
        salaryService.salaryCalculations(proposal(1000), null, null);

    assertThat(result.get("monthly").get("Gross Salary")).isCloseTo(1000, CENT);
    assertThat(result.get("yearly").get("Gross Salary")).isCloseTo(12000, CENT);
  }

  @Test
  void missingParametersDefaultToZero() {
    when(parameterService.getCalculationsParameters()).thenReturn(Map.of());

    Map<String, Double> monthly =
        salaryService.salaryCalculations(proposal(1000), null, null).get("monthly");

    assertThat(monthly.get("Base Salary")).isCloseTo(0, CENT);
    assertThat(monthly.get("IHT")).isCloseTo(0, CENT);
    assertThat(monthly.get("Share Purchase Plan")).isCloseTo(0, CENT);
  }

  @Test
  void rowsKeepTheBreakdownOrderWithExtrasBeforeNetSalary() {
    Extra food = extra("Food Allowance", 15, false, false, List.of(20L), 11, 21);

    Map<String, Map<String, Double>> result =
        salaryService.salaryCalculations(proposal(1000, food), null, null);

    assertThat(result.keySet()).containsExactly("monthly", "yearly");
    for (Map<String, Double> table : result.values()) {
      assertThat(table.keySet())
          .containsExactly(
              "Base Salary",
              "IHT",
              "Share Purchase Plan",
              "Gross Salary",
              "Food Allowance",
              "Estimated Net Salary");
    }
  }

  @Test
  void bracketedExtraUsesMarginalRateFromBaseSalary() {
    // Base 1000 sits in the 20% marginal bracket (next bracket after 1000).
    // A 100€ extra taxed by IRS should pay 20€, leaving 80€.
    Extra allowance = extra("Car Allowance", 100, false, false, List.of(20L), 12, 1);

    Map<String, Map<String, Double>> result =
        salaryService.salaryCalculations(proposal(1000, allowance), null, null);

    assertThat(result.get("monthly").get("Car Allowance")).isCloseTo(80, CENT);
    assertThat(result.get("yearly").get("Car Allowance")).isCloseTo(960, CENT);
  }

  @Test
  void withholdingTaxAppliesSingleBracketRateToWholeAmount() {
    incomeTax.setType(TaxType.WITHHOLDING);
    // Base 1500: withholding bracket 1000@10% does not cover it, next bracket 9999999@20% does.
    Map<String, Double> monthly =
        salaryService.salaryCalculations(proposal(1500), null, null).get("monthly");

    // SS = 165, IRS = 1500 * 20% = 300 -> net = 1500 - 465 = 1035
    assertThat(monthly.get("Estimated Net Salary")).isCloseTo(1035, CENT);
  }
}
