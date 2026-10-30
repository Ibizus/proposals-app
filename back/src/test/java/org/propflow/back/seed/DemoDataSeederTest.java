package org.propflow.back.seed;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.propflow.back.model.Candidate;
import org.propflow.back.model.Position;
import org.propflow.back.model.Proposal;
import org.propflow.back.model.TaxConfiguration;
import org.propflow.back.repository.CandidateRepository;
import org.propflow.back.repository.ExtraRepository;
import org.propflow.back.repository.ExtraTemplateRepository;
import org.propflow.back.repository.OfficeRepository;
import org.propflow.back.repository.ParameterRepository;
import org.propflow.back.repository.PositionRepository;
import org.propflow.back.repository.ProposalRepository;
import org.propflow.back.repository.SalaryRepository;
import org.propflow.back.repository.StatusRepository;
import org.propflow.back.repository.TaxConfigurationRepository;
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
  @Autowired private PositionRepository positionRepository;
  @Autowired private SalaryRepository salaryRepository;
  @Autowired private StatusRepository statusRepository;
  @Autowired private ExtraRepository extraRepository;
  @Autowired private ExtraTemplateRepository extraTemplateRepository;
  @Autowired private TaxConfigurationRepository taxConfigurationRepository;
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
            positionRepository,
            statusRepository,
            extraRepository,
            extraTemplateRepository,
            taxConfigurationRepository,
            parameterRepository);
  }

  private Map<String, Long> rowCounts() {
    Map<String, JpaRepository<?, Long>> repositories =
        Map.of(
            "offices", officeRepository,
            "candidates", candidateRepository,
            "proposals", proposalRepository,
            "positions", positionRepository,
            "salaries", salaryRepository,
            "statuses", statusRepository,
            "extras", extraRepository,
            "extraTemplates", extraTemplateRepository,
            "taxConfigurations", taxConfigurationRepository,
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
        .containsEntry("positions", (long) DemoDataSeeder.POSITION_COUNT)
        .containsEntry("salaries", (long) DemoDataSeeder.PROPOSAL_COUNT)
        .containsEntry("taxConfigurations", 2L)
        .containsEntry("parameters", 3L)
        .containsEntry("extraTemplates", 18L);

    TaxConfiguration active = taxConfigurationRepository.findByActiveTrue().orElseThrow();
    assertThat(active.getCountryCode()).isEqualTo("es");
    assertThat(active.getYearlyPayments()).isEqualTo(12);
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
              assertThat(p.getPosition()).isNotNull();
              assertThat(p.getPosition().getId()).isEqualTo(p.getCandidate().getPosition().getId());
              assertThat(p.getSalary().getBase()).isPositive();
              assertThat(p.getSalary().getExtras()).isNotEmpty();
              assertThat(p.getSalary().getTaxConfigurationId()).isNotNull();
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
  void spreadsCandidatesAcrossPositionsAndOnlyFinalStageCandidatesGetProposals() {
    seeder.seed();

    List<Candidate> candidates = candidateRepository.findAll(Sort.by("id"));
    assertThat(candidates).allMatch(c -> c.getPosition() != null);

    // Several candidates per position.
    Map<Long, Long> candidatesPerPosition =
        candidates.stream()
            .collect(Collectors.groupingBy(c -> c.getPosition().getId(), Collectors.counting()));
    assertThat(candidatesPerPosition).hasSize(DemoDataSeeder.POSITION_COUNT);
    assertThat(candidatesPerPosition.values()).allMatch(count -> count >= 2);

    // Proposals only for final-stage candidates; at most one proposal per candidate.
    Map<Boolean, Long> byProposalPresence =
        candidates.stream()
            .collect(
                Collectors.groupingBy(c -> !c.getProposals().isEmpty(), Collectors.counting()));
    assertThat(byProposalPresence)
        .containsEntry(true, (long) DemoDataSeeder.PROPOSAL_COUNT)
        .containsEntry(
            false, (long) (DemoDataSeeder.CANDIDATE_COUNT - DemoDataSeeder.PROPOSAL_COUNT));
    assertThat(candidates).allMatch(c -> c.getProposals().size() <= 1);

    assertThat(positionRepository.findAll()).extracting(Position::getNjoyn).doesNotHaveDuplicates();
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
            new HashSet<>(),
            null));

    seeder.seed();

    assertThat(proposalRepository.count()).isZero();
    assertThat(candidateRepository.count()).isEqualTo(1);
    assertThat(positionRepository.count()).isZero();
    assertThat(taxConfigurationRepository.count()).isEqualTo(2);
  }
}
