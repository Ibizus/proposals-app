package org.propflow.back.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.propflow.back.dto.ProposalDTO;
import org.propflow.back.exception.EntityNotFoundException;
import org.propflow.back.mapper.ProposalMapper;
import org.propflow.back.model.Candidate;
import org.propflow.back.model.Extra;
import org.propflow.back.model.Office;
import org.propflow.back.model.Proposal;
import org.propflow.back.model.Salary;
import org.propflow.back.model.Status;
import org.propflow.back.repository.CandidateRepository;
import org.propflow.back.repository.ExtraRepository;
import org.propflow.back.repository.OfficeRepository;
import org.propflow.back.repository.ProposalRepository;
import org.propflow.back.repository.SalaryRepository;
import org.propflow.back.util.UtilLazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class ProposalService {

  @Autowired
  private CandidateService candidateService;
  @Autowired
  private CandidateRepository candidateRepository;
  @SuppressWarnings("unused")
  @Autowired
  private OfficeRepository officeRepository;
  @Autowired
  private ProposalRepository proposalRepository;
  @Autowired
  private ProposalMapper proposalMapper;
  @Autowired
  private SalaryRepository salaryRepository;
  @Autowired
  private ExtraRepository extraRepository;
  @PersistenceContext
  EntityManager entityManager;

  public List<ProposalDTO> all() {
    List<Proposal> proposals = this.proposalRepository.findAll();

    return proposals.stream()
        .map(proposalMapper::toDTO)
        .collect(Collectors.toList());
  }

  // Pagination:
  public Map<String, Object> all(int page, int size) {
    Pageable paginator = PageRequest.of(page, size, Sort.by("id").ascending());
    Page<Proposal> pageAll = this.proposalRepository.findAll(paginator);

    Map<String, Object> response = new HashMap<>();

    // Map Proposals to ProposalsDTO before inserting data into Response Map:
    response.put("proposals", pageAll.getContent().stream()
        .map(proposalMapper::toDTO)
        .collect(Collectors.toList()));
    response.put("currentPage", pageAll.getNumber());
    response.put("totalItems", pageAll.getTotalElements());
    response.put("totalPages", pageAll.getTotalPages());
    return response;
  }

  // Filters by Name, Surname & Email of Candidate:
  public List<Proposal> findByCandidate(String name) {
    return this.proposalRepository
        .findProposalsByCandidate_NameContainingIgnoreCaseOrCandidate_SurnameContainingIgnoreCaseOrCandidate_EmailContainingIgnoreCase(
            name, name, name); // Same string will be checked against Name, Surname & Email fields.
  }

  // Find a Proposal by some fields (paginated):
  public Map<String, Object> findByFilter(int page, int size, String filter) {
    Pageable paginator = PageRequest.of(page, size, Sort.by("id").ascending());
    Page<Proposal> pageFiltered = this.proposalRepository
        .findProposalByNjoynOrDescriptionContainingIgnoreCaseOrRoleContainingIgnoreCaseOrCandidate_NameContainingIgnoreCaseOrCandidate_SurnameContainingIgnoreCaseOrCandidate_EmailContainingIgnoreCase(
            filter, filter, filter, filter, filter, filter, paginator);

    Map<String, Object> response = new HashMap<>();
    // Map Proposals to ProposalsDTO before inserting data into Response Map:
    response.put("proposals", pageFiltered.getContent().stream()
        .map(proposalMapper::toDTO)
        .collect(Collectors.toList()));
    response.put("currentPage", pageFiltered.getNumber());
    response.put("totalItems", pageFiltered.getTotalElements());
    response.put("totalPages", pageFiltered.getTotalPages());
    return response;
  }

  // Find proposals by Candidate Id:
  public List<ProposalDTO> findProposalsByCandidateId(Long id) {
    System.out.println("WE ARE NOW INSIDE PROPOSAL SERVICE");
    Candidate candidate = this.candidateService.one(id);
    System.out
        .println("Candidate found: " + candidate.getName() + " has " + candidate.getProposalsCount() + " proposals.");

    // Map Proposals to ProposalsDTO before returning:
    List<ProposalDTO> resultList = candidate.getProposals().stream()
        .map(proposalMapper::toDTO)
        .collect(Collectors.toList());

    return resultList;
  }

  public Proposal save(Proposal proposal, boolean isModifying) {
    Set<Extra> extras = proposal.getSalary().getExtras();
    Candidate candidate = proposal.getCandidate();
    Salary salary = proposal.getSalary();
    Office office = proposal.getOffice();

    UtilLazy.initializeLazyOneToManyByJoinFetch(entityManager,
        Office.class,
        Proposal.class,
        office.getId(),
        office::setProposals);

    Candidate persistedCandidate = candidateService.save(candidate);
    // Update proposal counter:
    persistedCandidate.setProposalsCount(persistedCandidate.getProposals().size() + 1);

    if (!isModifying) {
      salaryRepository.saveAndFlush(salary);
    }

    for (Extra extra : extras) {
      extra.setSalary(salary);
      extra.setTemplateId(extra.getId());
      extra.setId(0l);
      extraRepository.save(extra);
    }

    if (isModifying) {
      salaryRepository.save(salary);
    }
    // salary.setExtras(extras);
    salary.setProposal(proposal);

    // Save each candidate before adding them to the proposal
    proposal.setCandidate(persistedCandidate);
    proposal.setSalary(salary);
    office.getProposals().add(proposal);
    System.out.println(proposal.getNjoyn());

    return this.proposalRepository.save(proposal);
  }

  public Proposal one(Long id) {
    return this.proposalRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException(id, Proposal.class));
  }

  public Proposal replace(Long id, Proposal proposal) {
    Optional<Proposal> p = this.proposalRepository.findById(id);
    Proposal prop = new Proposal();
    if (p.isPresent()) {
      for (Extra extra : p.get().getSalary().getExtras()) {
        extra.setSalary(null);
        // p.get().getSalary().getExtras().remove(extra);
        this.extraRepository.delete(extra);
      }
      prop = save(proposal, true);
    }
    return prop;
  }

  public void delete(long proposalId) {
    Proposal proposal = proposalRepository.findById(proposalId)
        .orElseThrow(() -> new IllegalArgumentException("Proposal not found"));

    // Disassociate related entities
    if (proposal.getOffice() != null) {
      proposal.getOffice().getProposals().remove(proposal);
    }

    if (proposal.getSalary() != null) {
      proposal.getSalary().setProposal(null);
    }

    if (proposal.getCandidate() != null) {
      Candidate candidate = proposal.getCandidate();
      candidate.getProposals().remove(proposal);
      // Update proposal counter:
      candidate.setProposalsCount(candidate.getProposals().size());
    }

    // Now you can safely delete the proposal
    proposalRepository.delete(proposal);
  }

  public Map<String, Object> getDashboardSummary() {
    Map<String, Object> summary = new HashMap<>();
    summary.put("totalProposals", proposalRepository.count());
    summary.put("totalCandidates", candidateRepository.count());
    return summary;
  }

  public Map<String, Long> getProposalsByStatus() {
    List<Proposal> proposals = this.proposalRepository.findAll();
    Map<String, Long> statusMap = new HashMap<>();
    for (Proposal proposal : proposals) {
      Object[] objects = proposal.getStatuses().toArray();
      Status[] statuses = Arrays.stream(objects).map(o -> (Status) o).toArray(Status[]::new);
      // Sort the statuses by createdAt in ascending order
      Arrays.sort(statuses, Comparator.comparing(Status::getCreatedAt));
      Status latestStatus = statuses[statuses.length - 1];
      int status = latestStatus.getStatus();
      String res = "";
      switch (status) {
        case 0:
          res = "draft";
          break;
        case 1:
          res = "confirmed";
          break;
        case 2:
          res = "sent";
          break;
        case 3:
          res = "declined";
          break;
        case 4:
          res = "canceled";
          break;
        case 5:
          res = "approved";
          break;
        default:
          res = "UNKNOWN";
          break;
      }
      Long currentCount = statusMap.getOrDefault(res, 0L);
      statusMap.put(res, currentCount + 1L);
    }
    return statusMap;
  }

  public Map<String, Integer> getProposalsByCreatedAt() {
    List<Proposal> proposals = this.proposalRepository.findAll();

    // Use a TreeMap to maintain the order based on the key (date as String)
    Map<String, Integer> createdAtMap = new TreeMap<>();

    LocalDate minDate = null;
    LocalDate maxDate = null;

    for (Proposal proposal : proposals) {
      LocalDate createdAt = proposal.getCreatedAt().toLocalDate();
      String dateKey = createdAt.toString();
      Integer currentCount = createdAtMap.getOrDefault(dateKey, 0);
      createdAtMap.put(dateKey, currentCount + 1);

      // Determine min and max dates
      if (minDate == null || createdAt.isBefore(minDate)) {
        minDate = createdAt;
      }
      if (maxDate == null || createdAt.isAfter(maxDate)) {
        maxDate = createdAt;
      }
    }

    // Ensure minDate and maxDate are not null
    if (minDate != null && maxDate != null) {
      // Fill in missing dates with count 0
      LocalDate currentDate = minDate;
      while (!currentDate.isAfter(maxDate)) {
        String dateKey = currentDate.toString();
        createdAtMap.putIfAbsent(dateKey, 0);
        currentDate = currentDate.plusDays(1);
      }
    }

    return createdAtMap;
  }
}
