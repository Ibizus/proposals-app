package org.propflow.back.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "extra", indexes = @Index(name = "extra_name_index", columnList = "name"))
@Schema(description = "A salary extra such as a bonus or allowance.")
public class Extra {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private long id;

  private String name;
  private String description;
  private Double amount;
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

  private long templateId;

  @JsonBackReference
  @ManyToOne(optional = true)
  @JoinColumn(name = "salary_id")
  private Salary salary;

  // Constructor using template
  public Extra(ExtraTemplate template) {
    this.name = template.getName();
    this.amount = template.getAmount();
    this.ssTaxrate = template.getSsTaxrate();
    this.irsTaxrate = template.getIrsTaxrate();
    this.isPercentage = template.getIsPercentage();
    this.isSSTaxable = template.getIsSSTaxable();
    this.isIRSTaxable = template.getIsIRSTaxable();
    this.multiplyYearly = template.getMultiplyYearly();
    this.multiplyMonthly = template.getMultiplyMonthly();
  }
}
