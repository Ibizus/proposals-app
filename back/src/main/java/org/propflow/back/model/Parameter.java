package org.propflow.back.model;

import java.time.LocalDate;
import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper=false)
@Entity
@Table(name = "parameter",
      indexes = @Index(name = "param_name_index", columnList = "name"))
public class Parameter extends AuditableSuperclass{
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private long id;

  private String name;
  private Double value;
  private LocalDate validFrom;
  private LocalDate validUntil;
}
