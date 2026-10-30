package org.propflow.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

@Schema(description = "A country-specific tax configuration.")
public record TaxConfigurationDTO(
    Long id,
    @NotBlank @Schema(description = "ISO country code.", example = "es") String countryCode,
    @NotBlank @Schema(description = "Human-readable label.", example = "Spain (illustrative)")
        String label,
    @NotNull @Positive @Schema(description = "Number of payments per year.", example = "12")
        Integer yearlyPayments,
    @NotNull @Schema(description = "Whether this configuration is active.") Boolean active,
    @Valid @Schema(description = "Taxes that apply under this configuration.")
        List<TaxDTO> taxes) {}
