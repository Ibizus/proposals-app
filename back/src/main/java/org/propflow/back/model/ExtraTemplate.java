package org.propflow.back.model;

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
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(name = "extraTemplate", indexes = @Index(name = "exTemp_name_index", columnList = "name"))
@Schema(description = "A reusable template for salary extras.")
public class ExtraTemplate extends AuditableSuperclass
    implements org.propflow.back.migration.HasLegacyTaxFlags {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private long id;

  @NotBlank private String name;

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
  @CollectionTable(
      name = "extra_template_tax",
      joinColumns = @JoinColumn(name = "extra_template_id"))
  @Column(name = "tax_id")
  @Schema(
      description = "Taxes that apply to extras created from this template, ignored when exempt.")
  private Set<Long> taxIds = new HashSet<>();
}
