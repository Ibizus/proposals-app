package org.propflow.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.propflow.back.model.TaxType;

@Schema(description = "A tax inside a configuration.")
public record TaxDTO(
    Long id,
    @NotBlank @Schema(description = "Tax name.", example = "IRPF") String name,
    @NotNull @Schema(description = "FIXED or BRACKETED.", example = "BRACKETED") TaxType type,
    @Schema(description = "Fixed rate in percent. Only for FIXED taxes.", example = "6.45")
        Double fixedRate,
    @Schema(
            description = "When true, the tax only applies to extras, never to the base salary.",
            example = "false")
        Boolean extrasOnly,
    @Valid @Schema(description = "Progressive brackets. Only for BRACKETED taxes.")
        List<TaxBracketDTO> brackets) {}
