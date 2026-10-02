package org.propflow.back.model;

import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "salary",
      indexes = @Index(name = "base_index", columnList = "base"))
public class Salary{

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private long id;

  private Double base;

  @JsonBackReference
  @OneToOne(mappedBy = "salary", cascade = CascadeType.PERSIST)
  @JoinColumn(name = "proposal_id")
  private Proposal proposal;

  @JsonManagedReference
  @OneToMany(mappedBy = "salary")
  private Set<Extra> extras = new HashSet<>();
}