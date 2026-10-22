package org.propflow.back.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.HashSet;
import java.util.Set;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "salary", indexes = @Index(name = "base_index", columnList = "base"))
@Schema(description = "Base salary of a proposal.")
public class Salary {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private long id;

  @NotNull @Positive private Double base;

  @JsonBackReference
  @OneToOne(mappedBy = "salary", cascade = CascadeType.PERSIST)
  @JoinColumn(name = "proposal_id")
  private Proposal proposal;

  @JsonManagedReference
  @OneToMany(mappedBy = "salary")
  private Set<Extra> extras = new HashSet<>();
}
