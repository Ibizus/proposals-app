package org.propflow.back.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(name = "proposal", indexes = @Index(name = "nj_index", columnList = "njoyn"))
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
// @JsonSerialize(using = ProposalSerializer.class)
public class Proposal extends AuditableSuperclass {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private long id;

  private String njoyn;

  @NotBlank private String description;

  @NotBlank private String role;

  private Boolean indefinite;
  private String contractLevel;
  private LocalDate startDate;
  private LocalDate dueDate;
  private String knowMore;

  @JsonBackReference
  @ManyToOne
  @JoinColumn(name = "office_id")
  @NotNull
  @Valid
  private Office office;

  @JsonManagedReference
  @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinColumn(name = "salary_id")
  @NotNull
  @Valid
  private Salary salary;

  @ManyToOne
  @JoinColumn(name = "candidate_id") // Name of the foreign key column in the proposal table
  @NotNull
  @Valid
  private Candidate candidate;

  @JsonManagedReference
  @OneToMany(mappedBy = "proposal", cascade = CascadeType.ALL)
  private Set<Status> statuses = new HashSet<>();
}
