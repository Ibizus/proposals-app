package org.propflow.back.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import org.propflow.back.model.Candidate;
import org.propflow.back.model.Position;
import org.propflow.back.model.Salary;
import org.propflow.back.model.Status;

/**
 * This DTO was created to solve a problem where fetching a proposal would have an Office object
 * with a set of Proposals and it can result in an infinite loop. So ProposalDTO has an OfficeDTO
 * property instead of an Office, and the OfficeDTO has a set of the id's of the proposals it has,
 * so this should solve the infinite loop problem.
 */
@Schema(
    description =
        "Proposal payload returned by the API; the office is reduced to its identifiers to avoid reference cycles.")
public record ProposalDTO(
    long id,
    String description,
    String role,
    String contractLevel,
    Boolean indefinite,
    LocalDate startDate,
    LocalDate dueDate,
    @Schema(description = "Not populated; the current status is the most recent entry in statuses.")
        Integer status,
    @Schema(
            description = "Link to further details about the proposal.",
            example = "https://example.com/proposal-details")
        String knowMore,
    @Schema(description = "Office the proposal belongs to.") OfficeDTO office,
    Salary salary,
    Candidate candidate,
    @Schema(description = "Optional position this proposal belongs to; null for legacy proposals.")
        @JsonIgnoreProperties("candidates")
        Position position,
    @Schema(description = "Status history of the proposal; the latest entry is the current status.")
        Set<Status> statuses,
    @Schema(description = "Reference of the job posting in the nJoyn recruiting system.")
        String njoyn,
    LocalDateTime createdAt,
    String createdBy,
    LocalDateTime modifiedAt,
    String modifiedBy) {}
