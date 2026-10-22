package org.propflow.back.model;

import com.fasterxml.jackson.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
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
@Schema(description = "A job proposal for a candidate at an office.")
// @JsonSerialize(using = ProposalSerializer.class)
public class Proposal extends AuditableSuperclass {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private long id;

  @Schema(description = "Reference of the job posting in the nJoyn recruiting system.")
  private String njoyn;

  private String description;
  private String role;
  private Boolean indefinite;
  private String contractLevel;
  private LocalDate startDate;
  private LocalDate dueDate;
  private String knowMore;

  @JsonBackReference
  @ManyToOne
  @JoinColumn(name = "office_id")
  private Office office;

  @JsonManagedReference
  @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinColumn(name = "salary_id")
  private Salary salary;

  @ManyToOne
  @JoinColumn(name = "candidate_id") // Name of the foreign key column in the proposal table
  private Candidate candidate;

  @JsonManagedReference
  @OneToMany(mappedBy = "proposal", cascade = CascadeType.ALL)
  private Set<Status> statuses = new HashSet<>();
}
