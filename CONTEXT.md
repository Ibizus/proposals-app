# Context

## Proposal
A job proposal for a candidate at an office, containing role, description, contract type, dates, contract level, office, salary and candidate.

## Candidate
A person the proposal is made to (name, surname, id number, phone, email, worker id, marital status, dependents).

## Position
A job opening candidates are considered for, with title, description, nJoyn posting reference and a link to more details. Candidates are linked to a position, and a proposal may reference the position of its candidate.

## Proposal letter
The generated PDF document for a proposal, produced in-JVM from the proposal id, in one of es, pt, de, en.

## Extra
A salary component (bonus/allowance) attached to a proposal salary, with amount, applicable taxes and multipliers.

## Extra template
A reusable definition for a salary extra: name, amount (fixed or percentage), monthly and yearly multipliers, whether it adds to salary, whether it is tax exempt, and the taxes that apply. Templates belong to a tax configuration, and extras created from a template use that configuration's taxes.

## Office
The office/branch where the candidate will work.

## Salary breakdown
The computed monthly and yearly salary component amounts derived from a proposal.

## Parameter
A named salary calculation constant with a value and an optional validity period, read by the salary breakdown calculation.

## Status
The lifecycle state of a proposal (draft, confirmed, sent, declined, cancelled, approved).

## Tax configuration
A country-specific set of taxes and payment rules used to calculate salary breakdowns. Only one configuration is active at a time.

## Tax
A single tax inside a tax configuration: a fixed rate, a list of progressive brackets, or a withholding table. An extras-only tax applies only to the extras that reference it, never to the base salary.

## Tax bracket
A threshold/rate pair within a bracketed or withholding tax. For progressive brackets only the part of the base inside the bracket is taxed at that rate; for withholding the rate of the first bracket whose threshold reaches the base is applied to the whole base.
