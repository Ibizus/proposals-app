package org.propflow.back.seed;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.propflow.back.model.Candidate;
import org.propflow.back.model.Proposal;
import org.propflow.back.repository.CandidateRepository;
import org.propflow.back.repository.ExtraRepository;
import org.propflow.back.repository.ExtraTemplateRepository;
import org.propflow.back.repository.OfficeRepository;
import org.propflow.back.repository.ParameterRepository;
import org.propflow.back.repository.ProposalRepository;
import org.propflow.back.repository.SalaryRepository;
import org.propflow.back.repository.StatusRepository;
import org.propflow.back.repository.TaxesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

@DataJpaTest
// Use the H2 URL from src/test/resources (MySQL mode, VALUE allowed as a column name).
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DemoDataSeederTest {

  @Autowired private OfficeRepository officeRepository;
  @Autowired private CandidateRepository candidateRepository;
  @Autowired private ProposalRepository proposalRepository;
  @Autowired private SalaryRepository salaryRepository;
  @Autowired private StatusRepository statusRepository;
  @Autowired private ExtraRepository extraRepository;
  @Autowired private ExtraTemplateRepository extraTemplateRepository;
  @Autowired private TaxesRepository taxesRepository;
  @Autowired private ParameterRepository parameterRepository;

  private DemoDataSeeder seeder;

  @BeforeEach
  void setUp() {
    // Built by hand: the bean itself only exists when app.seed-demo-data=true.
    seeder =
        new DemoDataSeeder(
            officeRepository,
            candidateRepository,
            proposalRepository,
            statusRepository,
            extraRepository,
            extraTemplateRepository,
            taxesRepository,
            parameterRepository);
  }

  private Map<String, Long> rowCounts() {
    Map<String, JpaRepository<?, Long>> repositories =
        Map.of(
            "offices", officeRepository,
            "candidates", candidateRepository,
            "proposals", proposalRepository,
            "salaries", salaryRepository,
            "statuses", statusRepository,
            "extras", extraRepository,
            "extraTemplates", extraTemplateRepository,
            "taxes", taxesRepository,
            "parameters", parameterRepository);
    return repositories.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().count()));
  }

  @Test
  void seedsDemoDataOnceAndSkipsOnRepeatedRuns() {
    seeder.seed();
    Map<String, Long> afterFirstRun = rowCounts();

    seeder.seed();

    assertThat(rowCounts()).isEqualTo(afterFirstRun);
    assertThat(afterFirstRun)
        .containsEntry("proposals", (long) DemoDataSeeder.PROPOSAL_COUNT)
        .containsEntry("candidates", (long) DemoDataSeeder.CANDIDATE_COUNT)
        .containsEntry("salaries", (long) DemoDataSeeder.PROPOSAL_COUNT)
        .containsEntry("taxes", 105L)
        .containsEntry("parameters", 3L)
        .containsEntry("extraTemplates", 9L);
  }

  @Test
  void everyProposalHasItsRelationsFilledIn() {
    seeder.seed();

    List<Proposal> proposals = proposalRepository.findAll(Sort.by("id"));
    assertThat(proposals)
        .allSatisfy(
            p -> {
              assertThat(p.getOffice()).isNotNull();
              assertThat(p.getCandidate()).isNotNull();
              assertThat(p.getSalary().getBase()).isPositive();
              assertThat(p.getSalary().getExtras()).isNotEmpty();
              assertThat(p.getStatuses()).isNotEmpty();
              assertThat(p.getDueDate() == null).isEqualTo(p.getIndefinite());
            });
    assertThat(proposals).extracting(Proposal::getNjoyn).doesNotHaveDuplicates();
    // Statuses are stored per proposal, not collapsed into one row each.
    assertThat(statusRepository.count()).isGreaterThan(proposals.size());
    assertThat(statusRepository.findAll()).allMatch(s -> s.getProposal() != null);
    assertThat(officeRepository.findAll())
        .flatExtracting(o -> o.getProposals())
        .hasSize(DemoDataSeeder.PROPOSAL_COUNT);
    assertThat(candidateRepository.findAll())
        .extracting(Candidate::getEmail)
        .allMatch(email -> email.endsWith("@example.com"));
  }

  @Test
  void skipsDemoProposalsWhenTheDatabaseAlreadyHasData() {
    candidateRepository.save(
        new Candidate(
            0,
            "Existing",
            "Candidate",
            "ID-1",
            "910000000",
            "existing@example.com",
            "W-1",
            0,
            0,
            0,
            new HashSet<>(),
            new HashSet<>()));

    seeder.seed();

    assertThat(proposalRepository.count()).isZero();
    assertThat(candidateRepository.count()).isEqualTo(1);
    assertThat(taxesRepository.count()).isEqualTo(105);
  }
}
