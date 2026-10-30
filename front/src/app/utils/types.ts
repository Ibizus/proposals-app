interface Proposal {
  '@id': number;
  id: number;
  njoyn: string;
  description: string;
  role: string;
  contractLevel: string;
  startDate: string;
  dueDate: string;
  indefinite: boolean;
  knowMore: string;
  office: Office;
  salary: Salary;
  candidate: Candidate;
  position?: Position | null;
  statuses: Status[];
  createdAt: Date;
  createdBy: string;
  modifiedAt: Date;
  modifiedBy: string;
}

interface Office {
  id: number;
  name: string;
  code: string;
}

interface Salary {
  id: number;
  base: number;
  taxConfigurationId: number | null;
  extras: Extra[];
}

interface Extra {
  id: number;
  name: string;
  amount: number;
  isPercentage: boolean;
  isDefault: boolean;
  addToSalary: boolean;
  multiplyYearly: number;
  multiplyMonthly: number;
  exempt: boolean;
  taxIds: number[];
  createdAt: Date;
  createdBy: string;
  modifiedAt: Date;
  modifiedBy: string;
}

interface Candidate {
  '@id': number;
  id: number;
  name: string;
  surname: string;
  idNumber: string;
  phone: string;
  email: string;
  workerId: string;
  maritalStatus: number;
  dependentsNumber: number;
  nJoynSet: string[];
  proposals: number[];
  position?: Position | null;
}

interface Position {
  id: number;
  title: string;
  description: string;
  njoyn: string;
  knowMore: string;
  candidates?: number[];
  createdAt?: Date;
  createdBy?: string;
  modifiedAt?: Date;
  modifiedBy?: string;
}

interface TaxConfiguration {
  id: number;
  countryCode: string;
  label: string;
  yearlyPayments: number;
  active: boolean;
  taxes: Tax[];
  createdAt: Date;
  createdBy: string;
  modifiedAt: Date;
  modifiedBy: string;
}

interface Tax {
  id: number;
  name: string;
  type: 'FIXED' | 'BRACKETED';
  fixedRate: number | null;
  brackets: TaxBracket[];
  createdAt: Date;
  createdBy: string;
  modifiedAt: Date;
  modifiedBy: string;
}

interface TaxBracket {
  id: number;
  threshold: number;
  rate: number;
  createdAt: Date;
  createdBy: string;
  modifiedAt: Date;
  modifiedBy: string;
}

interface Taxes {
  id: number;
  salaryMax: number;
  socialSecurityTax: number;
  taxPercentageCompany: number;
  validFrom: Date;
  validUntil: Date;
  maritalStatus: number;
  zero: number;
  one: number;
  two: number;
  three: number;
  four: number;
  five: number;
  createdAt: Date;
  createdBy: string;
  modifiedAt: Date;
  modifiedBy: string;
}

interface Status {
  id: number;
  status: number;
  message: string;
  createdAt: Date;
  createdBy: string;
  modifiedAt: Date;
  modifiedBy: string;
}

interface Parameter {
  id: number;
  name: string;
  value: number;
  validFrom: Date;
  validUntil: Date;
  createdAt: Date;
  createdBy: string;
  modifiedAt: Date;
  modifiedBy: string;
}

interface Column {
  field: string;
  header: string;
}

interface PageEvent {
  first: number;
  rows: number;
  page: number;
  pageCount: number;
}

const EmptyProposalForm = () => {
  return {
    id: 0,
    role: '',
    description: '',
    njoyn: '',
    startDate: new Date(),
    dueDate: new Date(),
    contractLevel: '',
    knowMore: '',
    indefinite: true,
    office: {
      id: 0,
      name: '',
      code: '',
      proposals: [],
    },
    salary: {
      id: 0,
      base: 0,
      taxConfigurationId: null,
      extras: [],
    },
    candidate: {
      id: 0,
      name: '',
      surname: '',
      idNumber: '',
      phone: '',
      email: '',
      workerId: '',
      maritalStatus: '',
      dependentsNumber: 0,
    },
    position: null,
    statuses: [],
    createdAt: new Date(),
    createdBy: '',
    modifiedAt: new Date(),
    modifiedBy: '',
  };
};

export {
  Proposal,
  Office,
  Salary,
  Extra,
  Candidate,
  Position,
  Status,
  Taxes,
  TaxConfiguration,
  Tax,
  TaxBracket,
  Parameter,
  Column,
  PageEvent,
  EmptyProposalForm,
};
