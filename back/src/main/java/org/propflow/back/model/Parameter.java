package org.propflow.back.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(name = "parameter", indexes = @Index(name = "param_name_index", columnList = "name"))
@Schema(description = "A configurable salary parameter.")
public class Parameter extends AuditableSuperclass {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private long id;

  private String name;
  private Double value;
  private LocalDate validFrom;
  private LocalDate validUntil;
}
