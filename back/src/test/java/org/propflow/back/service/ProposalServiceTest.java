package org.propflow.back.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.propflow.back.mapper.ProposalMapper;
import org.propflow.back.model.Candidate;
import org.propflow.back.model.Office;
import org.propflow.back.model.Proposal;
import org.propflow.back.model.Salary;
import org.propflow.back.model.Status;
import org.propflow.back.repository.CandidateRepository;
import org.propflow.back.repository.ProposalRepository;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class ProposalServiceTest {

  private static final LocalDateTime T0 = LocalDateTime.of(2024, 4, 1, 9, 0);

  @Mock private ProposalRepository proposalRepository;
  @Mock private CandidateRepository candidateRepository;

  @Spy private ProposalMapper proposalMapper = new ProposalMapper();

  @InjectMocks private ProposalService proposalService;

  private static long nextStatusId = 1;

  // Status equality is id-based, so each one needs a distinct id to live in the same set.
  private static Status status(int code, LocalDateTime createdAt) {
    Status status = new Status(nextStatusId++, code, "status " + code, null);
    status.setCreatedAt(createdAt);
    return status;
  }

  private static Proposal proposal(long id, Status... statuses) {
    Proposal proposal = new Proposal();
    proposal.setId(id);
    proposal.setStatuses(new HashSet<>(Set.of(statuses)));
    return proposal;
  }

  private static Proposal createdOn(long id, LocalDateTime createdAt) {
    Proposal proposal = new Proposal();
    proposal.setId(id);
    proposal.setCreatedAt(createdAt);
    return proposal;
  }

  @Test
  void proposalsByStatusCountsOnlyTheLatestStatus() {
    List<Proposal> proposals =
        List.of(
            // draft -> confirmed -> sent: latest is "sent"
            proposal(1, status(0, T0), status(2, T0.plusHours(2)), status(1, T0.plusHours(1))),
            proposal(2, status(2, T0)),
            proposal(3, status(5, T0)),
            proposal(4, status(42, T0)));
    when(proposalRepository.findAll()).thenReturn(proposals);

    assertThat(proposalService.getProposalsByStatus())
        .containsOnly(Map.entry("sent", 2L), Map.entry("approved", 1L), Map.entry("UNKNOWN", 1L));
  }

  @Test
  void proposalsByCreatedAtFillsMissingDaysWithZero() {
    when(proposalRepository.findAll())
        .thenReturn(
            List.of(createdOn(1, T0), createdOn(2, T0.plusHours(3)), createdOn(3, T0.plusDays(3))));

    Map<String, Integer> byDay = proposalService.getProposalsByCreatedAt();

    assertThat(byDay.keySet())
        .containsExactly("2024-04-01", "2024-04-02", "2024-04-03", "2024-04-04");
    assertThat(byDay.values()).containsExactly(2, 0, 0, 1);
  }

  @Test
  void proposalsByCreatedAtIsEmptyWithoutProposals() {
    when(proposalRepository.findAll()).thenReturn(List.of());

    assertThat(proposalService.getProposalsByCreatedAt()).isEmpty();
  }

  @Test
  void dashboardSummaryCountsProposalsAndCandidates() {
    when(proposalRepository.count()).thenReturn(12L);
    when(candidateRepository.count()).thenReturn(5L);

    assertThat(proposalService.getDashboardSummary())
        .containsEntry("totalProposals", 12L)
        .containsEntry("totalCandidates", 5L);
  }

  @Test
  void deleteDetachesRelationsAndUpdatesCandidateCounter() {
    Proposal proposal = new Proposal();
    proposal.setId(1);
    Proposal other = new Proposal();
    other.setId(2);
    Office office = new Office(1, "Lisbon", "LIS", new HashSet<>(Set.of(proposal, other)));
    Salary salary = new Salary(1, 1000d, proposal, new HashSet<>());
    Candidate candidate = new Candidate();
    candidate.setProposals(new HashSet<>(Set.of(proposal, other)));
    candidate.setProposalsCount(2);
    proposal.setOffice(office);
    proposal.setSalary(salary);
    proposal.setCandidate(candidate);
    when(proposalRepository.findById(1L)).thenReturn(Optional.of(proposal));

    proposalService.delete(1L);

    assertThat(office.getProposals()).containsExactly(other);
    assertThat(salary.getProposal()).isNull();
    assertThat(candidate.getProposals()).containsExactly(other);
    assertThat(candidate.getProposalsCount()).isEqualTo(1);
    verify(proposalRepository).delete(proposal);
  }

  // Office and Salary point back at the proposal, like JPA-loaded entities do.
  private static Proposal linkedProposal(long id) {
    Proposal proposal = new Proposal();
    proposal.setId(id);
    proposal.setOffice(new Office(1, "Lisbon", "LIS", new HashSet<>(Set.of(proposal))));
    proposal.setSalary(new Salary(1, 1000d, proposal, new HashSet<>()));
    return proposal;
  }

  @Test
  void paginatedListHandlesEntityReferenceCycles() {
    when(proposalRepository.findAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(linkedProposal(1))));

    assertThat(proposalService.all(0, 10)).containsEntry("totalItems", 1L);
  }

  @Test
  void filteredListHandlesEntityReferenceCycles() {
    when(proposalRepository
            .findProposalByNjoynOrDescriptionContainingIgnoreCaseOrRoleContainingIgnoreCaseOrCandidate_NameContainingIgnoreCaseOrCandidate_SurnameContainingIgnoreCaseOrCandidate_EmailContainingIgnoreCase(
                "a", "a", "a", "a", "a", "a", PageRequest.of(0, 10, Sort.by("id").ascending())))
        .thenReturn(new PageImpl<>(List.of(linkedProposal(1))));

    assertThat(proposalService.findByFilter(0, 10, "a")).containsEntry("totalItems", 1L);
  }

  @Test
  void deleteRejectsUnknownProposal() {
    when(proposalRepository.findById(9L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> proposalService.delete(9L))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
