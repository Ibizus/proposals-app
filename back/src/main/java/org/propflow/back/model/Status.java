package org.propflow.back.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "status", indexes = @Index(name = "status_status_index", columnList = "status"))
public class Status extends AuditableSuperclass {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private long id;

  private int status;
  private String message;
  // private LocalDateTime date;

  @JsonBackReference
  @ManyToOne
  @JoinColumn(name = "proposal_id")
  private Proposal proposal;
}
