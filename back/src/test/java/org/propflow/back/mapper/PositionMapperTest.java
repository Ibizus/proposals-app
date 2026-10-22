package org.propflow.back.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.propflow.back.dto.PositionDTO;
import org.propflow.back.model.Candidate;
import org.propflow.back.model.Position;

class PositionMapperTest {

  private final PositionMapper mapper = new PositionMapper();

  @Test
  void mapsPositionFieldsAndFlattensCandidatesToIds() {
    Candidate first = new Candidate();
    first.setId(1);
    first.setName("Ann");
    Candidate second = new Candidate();
    second.setId(2);
    second.setName("Bob");
    Position position =
        new Position(
            3, "Backend", "Build services", "NJ-3", "more", new HashSet<>(Set.of(first, second)));

    PositionDTO dto = mapper.toDTO(position);

    assertThat(dto.id()).isEqualTo(3);
    assertThat(dto.title()).isEqualTo("Backend");
    assertThat(dto.description()).isEqualTo("Build services");
    assertThat(dto.njoyn()).isEqualTo("NJ-3");
    assertThat(dto.knowMore()).isEqualTo("more");
    assertThat(dto.candidates()).containsExactlyInAnyOrder(1L, 2L);
    assertThat(dto.createdAt()).isEqualTo(position.getCreatedAt());
    assertThat(dto.modifiedAt()).isEqualTo(position.getModifiedAt());
  }
}
