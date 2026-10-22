package org.propflow.back.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import org.junit.jupiter.api.Test;
import org.propflow.back.model.Candidate;
import org.propflow.back.model.Position;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
// Use the H2 URL from src/test/resources (MySQL mode, VALUE allowed as a column name).
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PositionRepositoryTest {

  @Autowired private PositionRepository positionRepository;
  @Autowired private CandidateRepository candidateRepository;

  private static Position position(String title) {
    return new Position(0, title, "description", "NJ-1", "more", new HashSet<>());
  }

  @Test
  void savesAndFindsPositionById() {
    Position saved = positionRepository.saveAndFlush(position("Backend"));

    assertThat(positionRepository.findById(saved.getId())).isPresent();
    assertThat(positionRepository.findById(saved.getId()).orElseThrow().getTitle())
        .isEqualTo("Backend");
  }

  @Test
  void persistenceDoesNotEnforceRequestValidationConstraints() {
    Position position = position("");
    position.setDescription(null);

    Position saved = positionRepository.saveAndFlush(position);

    assertThat(positionRepository.findById(saved.getId())).isPresent();
  }

  @Test
  void deletingCandidateDoesNotDeletePosition() {
    Position position = positionRepository.saveAndFlush(position("Backend"));
    Candidate candidate = new Candidate();
    candidate.setName("Ann");
    candidate.setPosition(position);
    candidateRepository.saveAndFlush(candidate);

    candidateRepository.delete(candidate);
    candidateRepository.flush();

    assertThat(positionRepository.findById(position.getId())).isPresent();
  }
}
