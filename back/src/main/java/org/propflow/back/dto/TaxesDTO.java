package org.propflow.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Computed IRS and social security amounts.")
public class TaxesDTO {

  private double irs;
  private double ss;
}
