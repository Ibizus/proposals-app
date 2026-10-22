package org.propflow.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** IRS and social security tax rates used by the salary calculation. */
@Schema(description = "IRS and social security tax rates applied to a salary.")
public record TaxesDTO(
    @Schema(description = "IRS (income tax) withholding rate, in percent.", example = "13.5")
        double irs,
    @Schema(description = "Employee social security contribution rate, in percent.", example = "11")
        double ss) {}
