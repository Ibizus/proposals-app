package org.propflow.back.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.propflow.back.model.Candidate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@DataJpaTest
// Use the H2 URL from src/test/resources (MySQL mode, VALUE allowed as a column name).
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CandidateRepositoryTest {

  @Autowired private CandidateRepository candidateRepository;

  private static Candidate candidate(
      String name, String surname, String idNumber, String email, String workerId) {
    return new Candidate(
        0,
        name,
        surname,
        idNumber,
        "600000000",
        email,
        workerId,
        0,
        0,
        0,
        new HashSet<>(),
        new HashSet<>());
  }

  @BeforeEach
  void setUp() {
    candidateRepository.save(candidate("Ann", "Smith", "ID-100", "ann@example.com", "W-1"));
    candidateRepository.save(candidate("Bob", "Annders", "ID-200", "bob@example.com", "W-2"));
    candidateRepository.save(candidate("Carl", "Jones", "ID-300", "carl@example.com", "W-3"));
  }

  private Page<Candidate> search(String term) {
    return candidateRepository
        .findCandidatesByNameContainingIgnoreCaseOrSurnameContainingIgnoreCaseOrIdNumberContainingIgnoreCaseOrPhoneContainingIgnoreCaseOrEmailContainingIgnoreCaseOrWorkerIdContainingIgnoreCase(
            term, term, term, term, term, term, PageRequest.of(0, 10, Sort.by("id").ascending()));
  }

  @Test
  void findsByIdNumber() {
    assertThat(candidateRepository.findCandidateByIdNumber("ID-200").getName()).isEqualTo("Bob");
    assertThat(candidateRepository.findCandidateByIdNumber("missing")).isNull();
  }

  @Test
  void filterMatchesAnyFieldIgnoringCase() {
    assertThat(search("ANN").getContent())
        .extracting(Candidate::getName)
        .containsExactly("Ann", "Bob");
    assertThat(search("id-300").getContent())
        .extracting(Candidate::getName)
        .containsExactly("Carl");
    assertThat(search("w-2").getContent()).extracting(Candidate::getName).containsExactly("Bob");
    assertThat(search("nobody").getContent()).isEmpty();
  }
}
