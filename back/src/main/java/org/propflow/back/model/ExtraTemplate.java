package org.propflow.back.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper=false)
@Entity
@Table(name = "extraTemplate",
      indexes = @Index(name = "exTemp_name_index", columnList = "name"))
public class ExtraTemplate extends AuditableSuperclass{

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private long id;

  private String name;
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
}