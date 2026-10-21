package org.propflow.back.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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

  @NotNull private Integer maritalStatus;

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
