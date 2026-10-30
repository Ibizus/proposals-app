# Context

## Proposal
A job proposal for a candidate at an office, containing role, description, contract type, dates, contract level, office, salary and candidate.

## Candidate
A person the proposal is made to (name, surname, id number, phone, email, worker id, marital status, dependents).

## Proposal letter
The generated PDF document for a proposal, produced in-JVM from the proposal id, in one of es, pt, de, en.

## Extra
A salary component (bonus/allowance) attached to a proposal salary, with amount, applicable taxes and multipliers.

## Office
The office/branch where the candidate will work.

## Salary breakdown
The computed monthly and yearly salary component amounts derived from a proposal.

## Status
The lifecycle state of a proposal (draft, confirmed, sent, declined, cancelled, approved).

## Tax configuration
A country-specific set of taxes and payment rules used to calculate salary breakdowns. Only one configuration is active at a time.

## Tax
A single tax inside a tax configuration: a fixed rate, a list of progressive brackets, or a withholding table. An extras-only tax applies only to the extras that reference it, never to the base salary.

## Tax bracket
A threshold/rate pair within a bracketed or withholding tax. For progressive brackets only the part of the base inside the bracket is taxed at that rate; for withholding the rate of the first bracket whose threshold reaches the base is applied to the whole base.
