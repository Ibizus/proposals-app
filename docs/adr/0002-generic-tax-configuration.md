# ADR 0002: Generic tax configuration instead of country-specific engines

## Status

Accepted

## Context

The salary calculation was originally hard-coded for a single country: fixed income tax brackets, a fixed social security rate and a fixed number of yearly payments lived in the backend code, so supporting another country meant adding another hard-coded configuration next to the existing one.

## Decision

The country-specific `Taxes` entity was replaced by a `TaxConfiguration` that holds the number of yearly salary payments and a list of `Tax` entries, so a configuration can be created per country's legislation. Each tax is either a fixed rate, a list of progressive brackets or a withholding table, and it can be marked extras-only. Each extra or salary item declares which taxes apply to it, or none when it is exempt. Marital status and dependents were removed from the tax model; candidate fields were left untouched.

Two configurations are preloaded as illustrative example data, Spain (active by default) and Portugal. Extra templates are scoped to a tax configuration, so extras created from a template reference taxes that exist in that configuration. Legacy per-extra social security and income tax rates are migrated into extras-only fixed-rate taxes of an existing tax configuration, so migrated proposals keep their amounts. Tax configurations can be created and activated but not replaced or deleted, so the tax ids that extras reference stay valid.
