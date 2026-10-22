package org.propflow.back.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(name = "taxes", indexes = @Index(name = "maxSal_index", columnList = "salaryMax"))
@Schema(description = "A tax bracket used for salary calculations.")
public class Taxes extends AuditableSuperclass {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private long id;

  @Schema(
      description =
          "Upper bound (inclusive) of the monthly gross salary covered by this bracket; a salary"
              + " uses the bracket with the smallest salaryMax that is not below it.",
      example = "1000")
  @NotNull
  @Positive
  private Double salaryMax;

  @NotNull @PositiveOrZero private Double socialSecurityTax;

  // private Double taxPercentageIRS;
  private Double taxPercentageCompany;
  private LocalDate validFrom;
  private LocalDate validUntil;

  @Schema(
      description =
          "Marital status the bracket applies to: 0 = single, 1 = married with one income, 2 ="
              + " married with two incomes.",
      allowableValues = {"0", "1", "2"},
      example = "0")
  @NotNull
  private Integer maritalStatus;

  // attributes for the IRS tax for each number of dependents
  private Double zero;
  private Double one;
  private Double two;
  private Double three;
  private Double four;
  private Double five;
}
