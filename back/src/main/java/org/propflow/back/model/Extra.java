package org.propflow.back.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "extra",
      indexes = @Index(name = "extra_name_index", columnList = "name"))
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
  private Boolean addToSalary;
  private int multiplyYearly;
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