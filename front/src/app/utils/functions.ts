import { Tax } from './types';

const INCOME_TAX_NAMES = ['IRS', 'IRPF', 'Income Tax'];
const SOCIAL_SECURITY_TAX_NAME = 'Social Security';

function isIncomeTax(tax: Tax): boolean {
  return INCOME_TAX_NAMES.includes(tax.name);
}

function isSocialSecurityTax(tax: Tax): boolean {
  return tax.name === SOCIAL_SECURITY_TAX_NAME;
}

function sleep(ms: number | undefined) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

export { isIncomeTax, isSocialSecurityTax, sleep };
