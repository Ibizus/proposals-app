package org.propflow.back.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.HashSet;
import java.util.Set;
import lombok.*;
import org.propflow.back.serializer.ProposalSerializer;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(name = "candidate", indexes = @Index(name = "idn_index", columnList = "idNumber"))
@Schema(description = "A candidate considered for one or more proposals.")
public class Candidate extends AuditableSuperclass {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private long id;

  @NotBlank private String name;

  @NotBlank private String surname;

  @NotBlank private String idNumber;

  @NotBlank private String phone;

  @NotBlank @Email private String email;

  private String workerId;

  @Schema(
      description =
          "Marital status used to pick the tax bracket: 0 = single, 1 = married with one income, 2"
              + " = married with two incomes.",
      allowableValues = {"0", "1", "2"},
      example = "0")
  @NotNull
  private Integer maritalStatus;

  @PositiveOrZero private Integer dependentsNumber;

  private Integer proposalsCount = 0;

  // Use of EAGER load strategy for this field because of its importance and
  // simplicity
  @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
  @CollectionTable(name = "nJoynSet", joinColumns = @JoinColumn(name = "candidate_id"))
  @Column(name = "nJoyn")
  private Set<String> nJoynSet = new HashSet<>();

  @JsonSerialize(using = ProposalSerializer.class)
  @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL)
  private Set<Proposal> proposals = new HashSet<>();
}
