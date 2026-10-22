package org.propflow.back.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Set;
import lombok.*;

/**
 * A job opening (position) that candidates are considered for. Several candidates can be linked to
 * the same position, and each candidate may or may not have a proposal for it depending on whether
 * they reach the final stage. The posting fields (title, description, njoyn, knowMore) are kept on
 * {@link Proposal} for now, so a position duplicates them until the frontend moves to reading them
 * from here.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
// "position" is a reserved word in MySQL, so the table is named job_position.
@Table(name = "job_position", indexes = @Index(name = "pos_njoyn_index", columnList = "njoyn"))
@Schema(description = "A job opening that candidates are considered for.")
public class Position extends AuditableSuperclass {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private long id;

  @Schema(description = "Job title or role of the position.", example = "Java Backend Developer")
  @NotBlank
  private String title;

  @NotBlank private String description;

  @Schema(description = "Reference of the job posting in the nJoyn recruiting system.")
  private String njoyn;

  @Schema(description = "Link to further details about the position.")
  private String knowMore;

  @JsonIgnoreProperties("position")
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @OneToMany(mappedBy = "position")
  private Set<Candidate> candidates = new HashSet<>();
}
