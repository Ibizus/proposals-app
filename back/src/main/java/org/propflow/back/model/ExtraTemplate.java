package org.propflow.back.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(name = "extraTemplate", indexes = @Index(name = "exTemp_name_index", columnList = "name"))
@Schema(description = "A reusable template for salary extras.")
public class ExtraTemplate extends AuditableSuperclass {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private long id;

  @NotBlank private String name;

  @NotNull private Double amount;
  private Double ssTaxrate;
  private Double irsTaxrate;
  private Boolean isPercentage;
  private Boolean isSSTaxable;
  private Boolean isIRSTaxable;
  private Boolean isDefault;

  @Schema(description = "Whether the extra amount is added to the net salary totals.")
  private Boolean addToSalary;

  @Schema(description = "Number of times the extra is paid per year.", example = "11")
  private int multiplyYearly;

  @Schema(
      description =
          "Number of times the extra is paid per month (for example working days), or -1 if it is"
              + " only paid yearly.",
      example = "22")
  private int multiplyMonthly;
}
