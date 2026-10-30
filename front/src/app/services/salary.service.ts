import { Injectable } from '@angular/core';
import { Extra, Proposal, Tax, TaxBracket, TaxConfiguration } from '../utils/types';
import { isIncomeTax, isSocialSecurityTax } from '../utils/functions';

interface TaxBracketRates {
  ss: number;
  irs: number;
  other?: number;
}

export interface TaxOverrides {
  irs?: number;
  ss?: number;
}

@Injectable({
  providedIn: 'root',
})
export class SalaryService {
  scrValue!: number;

  setGrossSalary(
    monthlySalaryCalculations: Map<string, number>,
    yearlySalaryCalculations: Map<string, number>,
    salary: number,
    payments: number,
  ): void {
    monthlySalaryCalculations.set('Gross Salary', salary);
    yearlySalaryCalculations.set('Gross Salary', salary * payments);
  }

  setBaseSalary(
    monthlySalaryCalculations: Map<string, number>,
    yearlySalaryCalculations: Map<string, number>,
    salary: number,
    iht: number,
    scr: number,
    payments: number,
  ): void {
    const baseSalary = salary * (1 - iht / 100) * scr;
    monthlySalaryCalculations.set('Base Salary', baseSalary);
    yearlySalaryCalculations.set('Base Salary', baseSalary * payments);
  }

  setIHT(
    monthlySalaryCalculations: Map<string, number>,
    yearlySalaryCalculations: Map<string, number>,
    salary: number,
    iht: number,
    payments: number,
  ): void {
    const ihtAmount = (salary * iht) / 100;
    monthlySalaryCalculations.set('IHT', ihtAmount);
    yearlySalaryCalculations.set('IHT', ihtAmount * payments);
  }

  setSharePurchasePlan(
    monthlySalaryCalculations: Map<string, number>,
    yearlySalaryCalculations: Map<string, number>,
    baseSalary: number,
    spp: number,
    payments: number,
  ): void {
    const sppValue = (baseSalary * spp) / 100;
    monthlySalaryCalculations.set('Share Purchase Plan', sppValue);
    yearlySalaryCalculations.set('Share Purchase Plan', sppValue * payments);
  }

  setNetSalary(
    proposal: Proposal,
    monthlySalaryCalculations: Map<string, number>,
    yearlySalaryCalculations: Map<string, number>,
    salary: number,
    taxBracket: TaxBracketRates,
    monthlyNetSalary: number,
    yearlyNetSalary: number,
    payments: number,
    configuration?: TaxConfiguration,
    overrides: TaxOverrides = {},
  ): void {
    const netSalary =
      salary -
      ((salary * taxBracket.ss) / 100 +
        (salary * taxBracket.irs) / 100 +
        (salary * (taxBracket.other ?? 0)) / 100);
    const monthlyNetSalaryNoExtras = netSalary;
    const yearlyNetSalaryNoExtras = netSalary * payments;
    [monthlyNetSalary, yearlyNetSalary] = this.setExtraSalaryComponents(
      proposal,
      monthlySalaryCalculations,
      yearlySalaryCalculations,
      monthlyNetSalaryNoExtras,
      yearlyNetSalaryNoExtras,
      salary,
      payments,
      configuration,
      overrides,
    );

    console.log('monthly -> ' + monthlyNetSalary);
    console.log('yearly -> ' + yearlyNetSalary);
    monthlySalaryCalculations.set('Estimated Net Salary', monthlyNetSalary);
    yearlySalaryCalculations.set('Estimated Net Salary', yearlyNetSalary);
  }

  setExtraSalaryComponents(
    proposal: Proposal,
    monthlySalaryCalculations: Map<string, number>,
    yearlySalaryCalculations: Map<string, number>,
    monthlyNetSalary: number,
    yearlyNetSalary: number,
    salary: number,
    payments: number,
    configuration?: TaxConfiguration,
    overrides: TaxOverrides = {},
  ): [number, number] {
    for (const extra of proposal.salary.extras) {
      const value = extra.isPercentage ? (salary * extra.amount) / 100 : extra.amount;

      const monthlyGross = extra.multiplyMonthly === -1 ? 0 : value * extra.multiplyMonthly;
      const yearlyGross =
        extra.multiplyMonthly === -1
          ? value * extra.multiplyYearly
          : monthlyGross * extra.multiplyYearly;
      const monthlyNet = this.netExtraValue(monthlyGross, extra, configuration, salary, overrides);
      const yearlyNet = this.netExtraValue(yearlyGross, extra, configuration, salary, overrides);

      if (extra.addToSalary === true) {
        monthlyNetSalary += monthlyNet;
        yearlyNetSalary += yearlyNet;
      }

      if (extra.multiplyMonthly !== -1) {
        // If monthly = -1 means that it is only Annual extra
        monthlySalaryCalculations.set(extra.name, monthlyNet);
      }
      yearlySalaryCalculations.set(extra.name, yearlyNet);
    }

    return [monthlyNetSalary, yearlyNetSalary];
  }

  private netExtraValue(
    value: number,
    extra: Extra,
    configuration: TaxConfiguration | undefined,
    baseSalary: number,
    overrides: TaxOverrides,
  ): number {
    if (extra.exempt || !configuration) {
      return value;
    }
    let tax = 0;
    for (const taxId of extra.taxIds) {
      const t = configuration.taxes.find((tx) => tx.id === taxId);
      if (!t) {
        throw new Error(
          `Tax ${taxId} of extra "${extra.name}" is not part of tax configuration ${configuration.id}`,
        );
      }
      tax += (value * this.marginalRate(t, baseSalary, this.overrideFor(t, overrides))) / 100;
    }
    return value - tax;
  }

  private overrideFor(tax: Tax, overrides: TaxOverrides): number | undefined {
    if (isIncomeTax(tax)) {
      return overrides.irs;
    }
    if (isSocialSecurityTax(tax)) {
      return overrides.ss;
    }
    return undefined;
  }

  private marginalRate(tax: Tax, baseSalary: number, override?: number): number {
    if (override != null) {
      return override;
    }
    if (tax.type === 'FIXED') {
      return tax.fixedRate ?? 0;
    }
    const sorted = [...tax.brackets].sort((a, b) => a.threshold - b.threshold);
    if (tax.type === 'WITHHOLDING') {
      const bracket = sorted.find((b) => baseSalary <= b.threshold) ?? sorted[sorted.length - 1];
      return bracket?.rate ?? 0;
    }
    for (const bracket of sorted) {
      if (baseSalary < bracket.threshold) {
        return bracket.rate;
      }
    }
    return sorted[sorted.length - 1]?.rate ?? 0;
  }

  calculateSCR(monthlySalaryCalculations: Map<string, number>, scr: number) {
    return (((monthlySalaryCalculations.get('Gross Salary') ?? 0) * 12) / 1000) * scr;
  }
}
