package org.propflow.back.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

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

  @Schema(
      description =
          "Tax configuration used for this salary. When null the active configuration is used.")
  private Long taxConfigurationId;

  @JsonBackReference
  @OneToOne(mappedBy = "salary", cascade = jakarta.persistence.CascadeType.PERSIST)
  @jakarta.persistence.JoinColumn(name = "proposal_id")
  private Proposal proposal;

  @JsonManagedReference
  @OneToMany(mappedBy = "salary")
  private Set<Extra> extras = new HashSet<>();
}
