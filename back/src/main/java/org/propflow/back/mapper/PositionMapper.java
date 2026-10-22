package org.propflow.back.mapper;

import java.util.Set;
import java.util.stream.Collectors;
import org.propflow.back.dto.PositionDTO;
import org.propflow.back.model.Candidate;
import org.propflow.back.model.Position;
import org.springframework.stereotype.Component;

/** Maps the Position model to the PositionDTO, reducing candidates to their ids. */
@Component
public class PositionMapper {

  public PositionDTO toDTO(Position position) {
    Set<Long> candidateIds =
        position.getCandidates().stream().map(Candidate::getId).collect(Collectors.toSet());

    return new PositionDTO(
        position.getId(),
        position.getTitle(),
        position.getDescription(),
        position.getNjoyn(),
        position.getKnowMore(),
        candidateIds,
        position.getCreatedAt(),
        position.getCreatedBy(),
        position.getModifiedAt(),
        position.getModifiedBy());
  }
}
