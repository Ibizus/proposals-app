package org.propflow.back.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "extra", indexes = @Index(name = "extra_name_index", columnList = "name"))
@Schema(description = "A salary extra such as a bonus or allowance.")
public class Extra implements org.propflow.back.migration.HasLegacyTaxFlags {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private long id;

  @NotBlank private String name;

  private String description;

  @NotNull private Double amount;

  @Deprecated
  @Schema(hidden = true)
  private Double ssTaxrate;

  @Deprecated
  @Schema(hidden = true)
  private Double irsTaxrate;

  private Boolean isPercentage;
  private Boolean isDefault;

  @Deprecated
  @Schema(hidden = true)
  private Boolean isSSTaxable;

  @Deprecated
  @Schema(hidden = true)
  private Boolean isIRSTaxable;

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

  @Schema(description = "When true, the extra is exempt from all taxes.")
  private Boolean exempt;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "extra_tax", joinColumns = @JoinColumn(name = "extra_id"))
  @Column(name = "tax_id")
  @Schema(description = "Taxes that apply to this extra, ignored when exempt.")
  private Set<Long> taxIds = new HashSet<>();

  private long templateId;

  @JsonBackReference
  @ManyToOne(optional = true)
  @JoinColumn(name = "salary_id")
  private Salary salary;

  // Constructor using template
  public Extra(ExtraTemplate template) {
    this.name = template.getName();
    this.amount = template.getAmount();
    this.isPercentage = template.getIsPercentage();
    this.multiplyYearly = template.getMultiplyYearly();
    this.multiplyMonthly = template.getMultiplyMonthly();
    this.exempt = template.getExempt();
    this.taxIds = new HashSet<>(template.getTaxIds());
  }
}
