package org.propflow.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(description = "A progressive tax bracket.")
public record TaxBracketDTO(
    Long id,
    @NotNull @PositiveOrZero @Schema(description = "Upper bound of the bracket.", example = "1500")
        Double threshold,
    @NotNull
        @PositiveOrZero
        @Schema(description = "Rate in percent for this bracket.", example = "15")
        Double rate) {}
