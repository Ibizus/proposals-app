package org.propflow.back.model;

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
public class Taxes extends AuditableSuperclass {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private long id;

  @NotNull @Positive private Double salaryMax;

  @NotNull @PositiveOrZero private Double socialSecurityTax;

  // private Double taxPercentageIRS;
  private Double taxPercentageCompany;
  private LocalDate validFrom;
  private LocalDate validUntil;

  @NotNull private Integer maritalStatus;
  // attributes for the IRS tax for each number of dependents
  private Double zero;
  private Double one;
  private Double two;
  private Double three;
  private Double four;
  private Double five;
}
