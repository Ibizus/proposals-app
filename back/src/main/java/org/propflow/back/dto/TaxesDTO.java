package org.propflow.back.dto;

/** IRS and social security tax rates used by the salary calculation. */
public record TaxesDTO(double irs, double ss) {}
