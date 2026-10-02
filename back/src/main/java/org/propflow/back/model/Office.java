package org.propflow.back.model;

import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "office",
      indexes = @Index(name = "ofi_name_index", columnList = "name"))
public class Office {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private long id;

  private String name;
  private String code;

  @JsonManagedReference
  @OneToMany(cascade = CascadeType.ALL)
  private Set<Proposal> proposals = new HashSet<>();
}
