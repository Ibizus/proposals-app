package org.propflow.back.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** One threshold/rate pair of a bracketed {@link Tax}. */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(
    name = "tax_bracket",
    indexes = @Index(name = "tax_bracket_tax_index", columnList = "tax_id"))
@Schema(description = "A progressive tax bracket: only income up to threshold is taxed at rate.")
public class TaxBracket extends AuditableSuperclass {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private long id;

  @NotNull
  @PositiveOrZero
  @Schema(
      description = "Upper bound (inclusive) of the monthly base covered by this bracket.",
      example = "1500")
  private Double threshold;

  @NotNull
  @PositiveOrZero
  @Schema(
      description = "Tax rate in percent for the part of the base inside the bracket.",
      example = "15")
  private Double rate;

  @NotNull @ManyToOne private Tax tax;
}
