package org.propflow.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Position payload returned by the API; candidates are reduced to their ids to avoid the reference
 * cycle between a position and its candidates.
 */
@Schema(
    description =
        "Position payload returned by the API; candidates are reduced to their identifiers to avoid reference cycles.")
public record PositionDTO(
    long id,
    String title,
    String description,
    @Schema(description = "Reference of the job posting in the nJoyn recruiting system.")
        String njoyn,
    String knowMore,
    @Schema(description = "Ids of the candidates linked to this position.") Set<Long> candidates,
    LocalDateTime createdAt,
    String createdBy,
    LocalDateTime modifiedAt,
    String modifiedBy) {}
