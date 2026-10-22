package org.propflow.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "IRS and social security tax rates applied to a salary.")
public class TaxesDTO {

  @Schema(description = "IRS (income tax) withholding rate, in percent.", example = "13.5")
  private double irs;

  @Schema(description = "Employee social security contribution rate, in percent.", example = "11")
  private double ss;
}
