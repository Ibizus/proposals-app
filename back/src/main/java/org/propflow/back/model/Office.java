package org.propflow.back.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "office", indexes = @Index(name = "ofi_name_index", columnList = "name"))
@Schema(description = "A company office that owns proposals.")
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
