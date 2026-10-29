import { Injectable } from "@angular/core";
import { Proposal } from "../utils/types";

interface TaxBracket {
  ss: number;
  irs: number;
}


@Injectable({
  providedIn: 'root',
})
export class SalaryService {
  scrValue!: number;

  setGrossSalary(
    monthlySalaryCalculations: Map<string, number>,
    yearlySalaryCalculations: Map<string, number>,
    salary: number
  ): void {
    monthlySalaryCalculations.set('Gross Salary', salary);
    yearlySalaryCalculations.set('Gross Salary', salary * 14);
  }
  
  setBaseSalary(
    monthlySalaryCalculations: Map<string, number>,
    yearlySalaryCalculations: Map<string, number>,
    salary: number,
    iht: number,
    scr: number
  ): void {
    const baseSalary = salary * (1 - iht / 100) * scr;
    monthlySalaryCalculations.set('Base Salary', baseSalary);
    yearlySalaryCalculations.set('Base Salary', baseSalary * 14);
  }
  
  setIHT(
    monthlySalaryCalculations: Map<string, number>,
    yearlySalaryCalculations: Map<string, number>,
    salary: number,
    iht: number
  ): void {
    const ihtAmount = (salary * iht) / 100;
    monthlySalaryCalculations.set('IHT', ihtAmount);
    yearlySalaryCalculations.set('IHT', ihtAmount * 14);
  }
  
  setSharePurchasePlan(
    monthlySalaryCalculations: Map<string, number>,
    yearlySalaryCalculations: Map<string, number>,
    baseSalary: number,
    spp: number
  ): void {
    const sppValue = (baseSalary * spp) / 100;
    monthlySalaryCalculations.set('Share Purchase Plan', sppValue);
    yearlySalaryCalculations.set('Share Purchase Plan', sppValue * 14);
  }
  
  setNetSalary(
    proposal: Proposal,
    monthlySalaryCalculations: Map<string, number>,
    yearlySalaryCalculations: Map<string, number>,
    salary: number,
    taxBracket: TaxBracket,
    monthlyNetSalary: number,
    yearlyNetSalary: number
  ): void {
    const netSalary =
      salary -
      ((salary * taxBracket.ss) / 100 +
        (salary * taxBracket.irs) / 100);
    const monthlyNetSalaryNoExtras = netSalary;
    const yearlyNetSalaryNoExtras = netSalary * 14;
    [monthlyNetSalary, yearlyNetSalary] = this.setExtraSalaryComponents(
      proposal,
      monthlySalaryCalculations,
      yearlySalaryCalculations,
      monthlyNetSalaryNoExtras,
      yearlyNetSalaryNoExtras,
      salary
    );

    console.log("monthly -> " + monthlyNetSalary);
    console.log("yearly -> " + yearlyNetSalary);
    monthlySalaryCalculations.set(
      'Estimated Net Salary',
      monthlyNetSalary
    );
    yearlySalaryCalculations.set(
      'Estimated Net Salary',
      yearlyNetSalary
    );

  }
  
  setExtraSalaryComponents(
    proposal: Proposal,
    monthlySalaryCalculations: Map<string, number>,
    yearlySalaryCalculations: Map<string, number>,
    monthlyNetSalary: number,
    yearlyNetSalary: number,
    salary: number
  ): [number, number] {
    for (const extra of proposal.salary.extras) {
      let value = extra.isPercentage ? (salary * extra.amount) / 100 : extra.amount;

      console.log("AddToSalary -> " + extra.addToSalary);

      if (extra.addToSalary === true) {
        const monthlyExtraSSTax = (value * extra.ssTaxrate) / 100;
        const monthlyExtraIRSTax = (value * extra.irsTaxrate) / 100;
        const monthlyNetExtraValue =
        value -
        (monthlyExtraSSTax * (extra.isSSTaxable ? 1 : 0) +
        monthlyExtraIRSTax * (extra.isIRSTaxable ? 1 : 0));
        monthlyNetSalary += (monthlyNetExtraValue * extra.multiplyMonthly);
        yearlyNetSalary +=
        monthlyNetExtraValue * (extra.multiplyMonthly * extra.multiplyYearly);
        console.log(monthlyNetSalary);
      }
      
      if (extra.multiplyMonthly != -1) {
        // If monthly = -1 means that it is only Annual extra
        value *= extra.multiplyMonthly;
        const monthlyExtraSSTax = (value * extra.ssTaxrate) / 100;
        const monthlyExtraIRSTax = (value * extra.irsTaxrate) / 100;
        const monthlyNetExtraValue =
        value -
        (monthlyExtraSSTax * (extra.isSSTaxable ? 1 : 0) +
        monthlyExtraIRSTax * (extra.isIRSTaxable ? 1 : 0));
        
        monthlySalaryCalculations.set(extra.name, monthlyNetExtraValue);
        yearlySalaryCalculations.set(
          extra.name,
          monthlyNetExtraValue * extra.multiplyYearly
        );
      } else {
        value *= extra.multiplyYearly;
        const yearlyExtraSSTax = (value * extra.ssTaxrate) / 100;
        const yearlyExtraIRSTax = (value * extra.irsTaxrate) / 100;
        const yearlyNetExtraValue =
        value -
        (yearlyExtraSSTax * (extra.isSSTaxable ? 1 : 0) +
        yearlyExtraIRSTax * (extra.isIRSTaxable ? 1 : 0));
        yearlySalaryCalculations.set(extra.name, yearlyNetExtraValue);
      }
    }

    return [monthlyNetSalary, yearlyNetSalary];
  }

  calculateSCR(
    monthlySalaryCalculations: Map<string, number>,
    scr: number,
  ){
    return (((monthlySalaryCalculations.get('Gross Salary') ?? 0) * 12) / 1000) * scr;
  }
}