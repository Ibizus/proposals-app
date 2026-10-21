package org.propflow.back.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import org.propflow.back.model.Candidate;
import org.propflow.back.model.Salary;
import org.propflow.back.model.Status;

/**
 * This DTO was created to solve a problem where fetching a proposal would have an Office object
 * with a set of Proposals and it can result in an infinite loop. So ProposalDTO has an OfficeDTO
 * property instead of an Office, and the OfficeDTO has a set of the ids of the proposals it has, so
 * this should solve the infinite loop problem.
 */
public record ProposalDTO(
    long id,
    String description,
    String role,
    String contractLevel,
    Boolean indefinite,
    LocalDate startDate,
    LocalDate dueDate,
    String knowMore,
    OfficeDTO office,
    Salary salary,
    Candidate candidate,
    Set<Status> statuses,
    String njoyn,
    LocalDateTime createdAt,
    String createdBy,
    LocalDateTime modifiedAt,
    String modifiedBy) {}
