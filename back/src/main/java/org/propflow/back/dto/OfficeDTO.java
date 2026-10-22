package org.propflow.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

/**
 * This DTO was created to solve a problem where fetching a proposal would have an Office object
 * with a set of Proposals and it can result in an infinite loop. So ProposalDTO has an OfficeDTO
 * property instead of an Office, and the OfficeDTO has a set of the id's of the proposals it has,
 * so this should solve the infinite loop problem.
 */
@Schema(
    description =
        "Office payload returned inside proposals, referencing proposal ids to avoid cycles.")
public record OfficeDTO(
    long id,
    String name,
    String code,
    @Schema(description = "Ids of the proposals that belong to this office.")
        Set<Long> proposals) {}
