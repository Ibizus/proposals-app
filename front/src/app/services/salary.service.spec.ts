import { TestBed } from '@angular/core/testing';
import { SalaryService } from './salary.service';
import { makeExtra, makeProposal, makeTax, makeTaxConfiguration } from '../../testing/fixtures';
import { Tax } from '../utils/types';

describe('SalaryService', () => {
  let service: SalaryService;
  let monthly: Map<string, number>;
  let yearly: Map<string, number>;
  const payments = 14;

  beforeEach(() => {
    service = TestBed.inject(SalaryService);
    monthly = new Map();
    yearly = new Map();
  });

  it('sets gross salary using the configurable payments count', () => {
    service.setGrossSalary(monthly, yearly, 1000, 12);
    expect(monthly.get('Gross Salary')).toBe(1000);
    expect(yearly.get('Gross Salary')).toBe(12000);
  });

  it('derives base salary from IHT and SCR', () => {
    service.setBaseSalary(monthly, yearly, 1000, 10, 1.5, payments);
    expect(monthly.get('Base Salary')).toBeCloseTo(1350, 6);
    expect(yearly.get('Base Salary')).toBeCloseTo(18900, 6);
  });

  it('computes the IHT amount as a percentage of salary', () => {
    service.setIHT(monthly, yearly, 1000, 10, payments);
    expect(monthly.get('IHT')).toBe(100);
    expect(yearly.get('IHT')).toBe(1400);
  });

  it('computes the share purchase plan as a percentage of salary', () => {
    service.setSharePurchasePlan(monthly, yearly, 1000, 5, payments);
    expect(monthly.get('Share Purchase Plan')).toBe(50);
    expect(yearly.get('Share Purchase Plan')).toBe(700);
  });

  it('computes SCR from yearly (12 months) gross salary', () => {
    monthly.set('Gross Salary', 1000);
    expect(service.calculateSCR(monthly, 2)).toBe(24);
  });

  it('returns 0 SCR when gross salary is missing', () => {
    expect(service.calculateSCR(monthly, 2)).toBe(0);
  });

  describe('setExtraSalaryComponents', () => {
    const config = makeTaxConfiguration();

    it('records a taxed monthly extra without touching net salary', () => {
      const proposal = makeProposal({
        salary: { id: 1, base: 1000, taxConfigurationId: null, extras: [makeExtra()] },
      });

      const [m, y] = service.setExtraSalaryComponents(proposal, monthly, yearly, 500, 7000, 1000, payments, config);

      // 100 - (11% SS + 20% IRS) = 69 per month, 11 months a year
      expect(monthly.get('Meal allowance')).toBeCloseTo(69, 6);
      expect(yearly.get('Meal allowance')).toBeCloseTo(759, 6);
      expect([m, y]).toEqual([500, 7000]);
    });

    it('adds the net extra to net salary when addToSalary is set', () => {
      const proposal = makeProposal({
        salary: { id: 1, base: 1000, taxConfigurationId: null, extras: [makeExtra({ addToSalary: true })] },
      });

      const [m, y] = service.setExtraSalaryComponents(proposal, monthly, yearly, 500, 7000, 1000, payments, config);

      // 100 - (11% SS + 20% IRS) = 69
      expect(m).toBeCloseTo(569, 6);
      expect(y).toBeCloseTo(7759, 6);
    });

    it('computes percentage extras from the base salary', () => {
      const proposal = makeProposal({
        salary: {
          id: 1,
          base: 1000,
          taxConfigurationId: null,
          extras: [
            makeExtra({ isPercentage: true, amount: 10, taxIds: [], exempt: true }),
          ],
        },
      });

      service.setExtraSalaryComponents(proposal, monthly, yearly, 0, 0, 1000, payments, config);

      expect(monthly.get('Meal allowance')).toBe(100);
      expect(yearly.get('Meal allowance')).toBe(1100);
    });

    it('records annual-only extras (multiplyMonthly = -1) only in yearly totals', () => {
      const proposal = makeProposal({
        salary: {
          id: 1,
          base: 1000,
          taxConfigurationId: null,
          extras: [
            makeExtra({
              name: 'Bonus',
              amount: 500,
              multiplyMonthly: -1,
              multiplyYearly: 2,
              taxIds: [20],
            }),
          ],
        },
      });

      service.setExtraSalaryComponents(proposal, monthly, yearly, 0, 0, 1000, payments, config);

      // 500 * 2 = 1000, minus 20% IRS
      expect(monthly.has('Bonus')).toBeFalse();
      expect(yearly.get('Bonus')).toBe(800);
    });

    it('treats exempt extras as untaxed', () => {
      const proposal = makeProposal({
        salary: {
          id: 1,
          base: 1000,
          taxConfigurationId: null,
          extras: [makeExtra({ exempt: true, taxIds: [] })],
        },
      });

      service.setExtraSalaryComponents(proposal, monthly, yearly, 0, 0, 1000, payments, config);

      expect(monthly.get('Meal allowance')).toBe(100);
    });
  });

  it('sets estimated net salary after SS and IRS withholding', () => {
    const proposal = makeProposal();

    service.setNetSalary(proposal, monthly, yearly, 1000, { ss: 11, irs: 20 }, 0, 0, payments);

    expect(monthly.get('Estimated Net Salary')).toBeCloseTo(690, 6);
    expect(yearly.get('Estimated Net Salary')).toBeCloseTo(9660, 6);
  });
});
