package org.propflow.back.service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.propflow.back.model.Extra;
import org.propflow.back.model.Proposal;
import org.propflow.back.model.Salary;
import org.propflow.back.model.Tax;
import org.propflow.back.model.TaxBracket;
import org.propflow.back.model.TaxConfiguration;
import org.propflow.back.model.TaxType;
import org.propflow.back.repository.SalaryRepository;
import org.propflow.back.repository.TaxConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SalaryService {

  @Autowired private SalaryRepository salaryRepository;
  @Autowired private ParameterService parameterService;
  @Autowired private TaxConfigurationRepository taxConfigurationRepository;

  private double scrValue;

  // PERSISTENCE METHODS USING REPOSITORY:
  public List<Salary> all() {
    return this.salaryRepository.findAll();
  }

  public Salary save(Salary salary) {
    return this.salaryRepository.save(salary);
  }

  public Salary one(Long id) {
    return this.salaryRepository
        .findById(id)
        .orElseThrow(
            () -> new org.propflow.back.exception.EntityNotFoundException(id, Salary.class));
  }

  public Salary replace(Long id, Salary salary) {
    return this.salaryRepository
        .findById(id)
        .map(
            b -> {
              if (id.equals(salary.getId())) return this.salaryRepository.save(salary);
              else
                throw new org.propflow.back.exception.NotCouplingIdException(
                    id, salary.getId(), Salary.class);
            })
        .orElseThrow(
            () -> new org.propflow.back.exception.EntityNotFoundException(id, Salary.class));
  }

  public void delete(Long id) {
    this.salaryRepository
        .findById(id)
        .map(
            b -> {
              this.salaryRepository.delete(b);
              return b;
            })
        .orElseThrow(
            () -> new org.propflow.back.exception.EntityNotFoundException(id, Salary.class));
  }

  // SALARY CALCULATION METHODS:

  /**
   * Calculates the monthly and yearly salary breakdown for a proposal using its tax configuration.
   * The {@code irs} and {@code ss} parameters are legacy overrides applied to the income tax and
   * social security taxes when they exist in the active configuration.
   */
  public Map<String, Map<String, Double>> salaryCalculations(
      Proposal proposal, Double irs, Double ss) {

    Map<String, Map<String, Double>> calculations = new LinkedHashMap<>();
    Map<String, Double> monthlySalaryCalculations = new LinkedHashMap<>();
    Map<String, Double> yearlySalaryCalculations = new LinkedHashMap<>();
    double salary = proposal.getSalary().getBase();

    TaxConfiguration configuration = resolveConfiguration(proposal.getSalary());
    int payments = configuration.getYearlyPayments();

    Map<Long, Double> overrides = buildOverrides(configuration, irs, ss);

    Map<String, Double> parameters = this.parameterService.getCalculationsParameters();
    double iht = parameters.getOrDefault("iht", 0.0);
    double spp = parameters.getOrDefault("spp", 0.0);
    double scr = parameters.getOrDefault("scr", 0.0);

    setBaseSalary(monthlySalaryCalculations, yearlySalaryCalculations, salary, iht, scr, payments);
    setIHT(monthlySalaryCalculations, yearlySalaryCalculations, salary, iht, payments);
    setSharePurchasePlan(
        monthlySalaryCalculations, yearlySalaryCalculations, salary, spp, payments);
    setGrossSalary(monthlySalaryCalculations, yearlySalaryCalculations, salary, payments);
    setNetSalary(
        proposal,
        monthlySalaryCalculations,
        yearlySalaryCalculations,
        salary,
        configuration,
        overrides,
        payments);

    this.scrValue = this.calculateSCR(monthlySalaryCalculations, scr);

    calculations.put("monthly", monthlySalaryCalculations);
    calculations.put("yearly", yearlySalaryCalculations);
    return calculations;
  }

  private TaxConfiguration resolveConfiguration(Salary salary) {
    Long configId = salary.getTaxConfigurationId();
    if (configId != null) {
      return taxConfigurationRepository.findById(configId).orElseGet(this::findActiveConfiguration);
    }
    return findActiveConfiguration();
  }

  private TaxConfiguration findActiveConfiguration() {
    return taxConfigurationRepository
        .findByActiveTrue()
        .orElseThrow(() -> new IllegalStateException("No active tax configuration"));
  }

  private Map<Long, Double> buildOverrides(TaxConfiguration configuration, Double irs, Double ss) {
    Map<Long, Double> overrides = new HashMap<>();
    if (irs != null) {
      findIncomeTax(configuration).ifPresent(tax -> overrides.put(tax.getId(), irs));
    }
    if (ss != null) {
      findSocialSecurityTax(configuration).ifPresent(tax -> overrides.put(tax.getId(), ss));
    }
    return overrides;
  }

  private Optional<Tax> findIncomeTax(TaxConfiguration configuration) {
    return configuration.getTaxes().stream()
        .filter(t -> "IRS".equals(t.getName()) || "IRPF".equals(t.getName()))
        .findFirst();
  }

  private Optional<Tax> findSocialSecurityTax(TaxConfiguration configuration) {
    return configuration.getTaxes().stream()
        .filter(t -> "Social Security".equals(t.getName()))
        .findFirst();
  }

  private double calculateTax(Tax tax, double base, Double override) {
    if (tax.getType() == TaxType.FIXED) {
      double rate = override != null ? override : tax.getFixedRate();
      return base * rate / 100;
    }
    if (override != null) {
      return base * override / 100;
    }
    if (tax.getType() == TaxType.WITHHOLDING) {
      return base * withholdingRate(tax, base) / 100;
    }
    double total = 0;
    double remaining = base;
    double previousThreshold = 0;
    for (TaxBracket bracket : tax.getBrackets()) {
      double bracketSize = bracket.getThreshold() - previousThreshold;
      double taxableInBracket = Math.min(remaining, Math.max(0, bracketSize));
      total += taxableInBracket * bracket.getRate() / 100;
      remaining -= taxableInBracket;
      if (remaining <= 0) break;
      previousThreshold = bracket.getThreshold();
    }
    return total;
  }

  /**
   * Returns the marginal tax rate (as a percentage) that applies to the next euro earned on top of
   * {@code base}. Used for extras so they are taxed at the same marginal rate as the salary they
   * are added to, rather than as standalone income.
   */
  private double marginalRate(Tax tax, double base, Double override) {
    if (override != null) {
      return override;
    }
    if (tax.getType() == TaxType.FIXED) {
      return tax.getFixedRate();
    }
    if (tax.getType() == TaxType.WITHHOLDING) {
      return withholdingRate(tax, base);
    }
    return marginalBracketRate(tax, base);
  }

  private double marginalBracketRate(Tax tax, double base) {
    for (TaxBracket bracket : tax.getBrackets()) {
      if (base < bracket.getThreshold()) {
        return bracket.getRate();
      }
    }
    List<TaxBracket> brackets = tax.getBrackets();
    return brackets.isEmpty() ? 0.0 : brackets.get(brackets.size() - 1).getRate();
  }

  private double withholdingRate(Tax tax, double base) {
    for (TaxBracket bracket : tax.getBrackets()) {
      if (base <= bracket.getThreshold()) {
        return bracket.getRate();
      }
    }
    List<TaxBracket> brackets = tax.getBrackets();
    return brackets.isEmpty() ? 0.0 : brackets.get(brackets.size() - 1).getRate();
  }

  private Tax findTaxById(TaxConfiguration configuration, long taxId) {
    return configuration.getTaxes().stream()
        .filter(t -> t.getId() == taxId)
        .findFirst()
        .orElse(null);
  }

  // METHODS USED INSIDE SALARY CALCULATIONS:
  private void setGrossSalary(
      Map<String, Double> monthlySalaryCalculations,
      Map<String, Double> yearlySalaryCalculations,
      double salary,
      int payments) {
    monthlySalaryCalculations.put("Gross Salary", salary);
    yearlySalaryCalculations.put("Gross Salary", salary * payments);
  }

  private void setBaseSalary(
      Map<String, Double> monthlySalaryCalculations,
      Map<String, Double> yearlySalaryCalculations,
      double salary,
      double iht,
      double scr,
      int payments) {
    double baseSalary = salary * (1 - iht / 100) * scr;
    monthlySalaryCalculations.put("Base Salary", baseSalary);
    yearlySalaryCalculations.put("Base Salary", baseSalary * payments);
  }

  private void setIHT(
      Map<String, Double> monthlySalaryCalculations,
      Map<String, Double> yearlySalaryCalculations,
      double salary,
      double iht,
      int payments) {
    double ihtAmount = (salary * iht) / 100;
    monthlySalaryCalculations.put("IHT", ihtAmount);
    yearlySalaryCalculations.put("IHT", ihtAmount * payments);
  }

  private void setSharePurchasePlan(
      Map<String, Double> monthlySalaryCalculations,
      Map<String, Double> yearlySalaryCalculations,
      double baseSalary,
      double spp,
      int payments) {
    double sppValue = (baseSalary * spp) / 100;
    monthlySalaryCalculations.put("Share Purchase Plan", sppValue);
    yearlySalaryCalculations.put("Share Purchase Plan", sppValue * payments);
  }

  private void setNetSalary(
      Proposal proposal,
      Map<String, Double> monthlySalaryCalculations,
      Map<String, Double> yearlySalaryCalculations,
      double salary,
      TaxConfiguration configuration,
      Map<Long, Double> overrides,
      int payments) {
    double totalTax = 0;
    for (Tax tax : configuration.getTaxes()) {
      totalTax += calculateTax(tax, salary, overrides.get(tax.getId()));
    }
    double netSalary = salary - totalTax;
    double monthlyNetSalary = netSalary;
    double yearlyNetSalary = netSalary * payments;

    double[] totals =
        setExtraSalaryComponents(
            proposal,
            monthlySalaryCalculations,
            yearlySalaryCalculations,
            monthlyNetSalary,
            yearlyNetSalary,
            salary,
            configuration,
            overrides,
            payments);

    monthlyNetSalary = totals[0];
    yearlyNetSalary = totals[1];

    monthlySalaryCalculations.put("Estimated Net Salary", monthlyNetSalary);
    yearlySalaryCalculations.put("Estimated Net Salary", yearlyNetSalary);
  }

  private double[] setExtraSalaryComponents(
      Proposal proposal,
      Map<String, Double> monthlySalaryCalculations,
      Map<String, Double> yearlySalaryCalculations,
      double monthlyNetSalary,
      double yearlyNetSalary,
      double salary,
      TaxConfiguration configuration,
      Map<Long, Double> overrides,
      int payments) {
    for (Extra extra : proposal.getSalary().getExtras()) {
      double amount = extra.getAmount();
      double baseValue =
          Boolean.TRUE.equals(extra.getIsPercentage()) ? (salary * amount) / 100 : amount;

      int multiplyMonthly = extra.getMultiplyMonthly();
      int multiplyYearly = extra.getMultiplyYearly();

      double monthlyGross = multiplyMonthly == -1 ? 0.0 : baseValue * multiplyMonthly;
      double yearlyGross =
          multiplyMonthly == -1 ? baseValue * multiplyYearly : monthlyGross * multiplyYearly;

      double monthlyTax = taxOnExtra(monthlyGross, salary, extra, configuration, overrides);
      double yearlyTax = taxOnExtra(yearlyGross, salary, extra, configuration, overrides);
      double monthlyNet = monthlyGross - monthlyTax;
      double yearlyNet = yearlyGross - yearlyTax;

      if (Boolean.TRUE.equals(extra.getAddToSalary())) {
        monthlyNetSalary += monthlyNet;
        yearlyNetSalary += yearlyNet;
      }

      if (multiplyMonthly != -1) {
        monthlySalaryCalculations.put(extra.getName(), monthlyNet);
      }
      yearlySalaryCalculations.put(extra.getName(), yearlyNet);
    }
    return new double[] {monthlyNetSalary, yearlyNetSalary};
  }

  private double taxOnExtra(
      double value,
      double baseSalary,
      Extra extra,
      TaxConfiguration configuration,
      Map<Long, Double> overrides) {
    if (Boolean.TRUE.equals(extra.getExempt())) return 0;
    double total = 0;
    for (Long taxId : extra.getTaxIds()) {
      Tax tax = findTaxById(configuration, taxId);
      if (tax != null) {
        double rate = marginalRate(tax, baseSalary, overrides.get(tax.getId()));
        total += value * rate / 100;
      }
    }
    return total;
  }

  // Calculates the SCR value using the predefined formula (GrossMonthlySalary*12/1000)*SCR)
  private double calculateSCR(Map<String, Double> monthlySalaryCalculations, Double scr) {
    return ((monthlySalaryCalculations.get("Gross Salary") * 12) / 1000) * scr;
  }

  public double getScrValue() {
    return this.scrValue;
  }
}
