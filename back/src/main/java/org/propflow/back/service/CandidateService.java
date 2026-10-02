package org.propflow.back.service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import org.propflow.back.exception.EntityNotFoundException;
import org.propflow.back.exception.NotCouplingIdException;
import org.propflow.back.model.Candidate;
import org.propflow.back.model.Office;
import org.propflow.back.model.Proposal;
import org.propflow.back.repository.CandidateRepository;
import org.propflow.back.repository.OfficeRepository;
import org.propflow.back.repository.ProposalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class CandidateService {

  @Autowired
  private CandidateRepository candidateRepository;
  @Autowired
  private ProposalRepository proposalRepository;
  @Autowired
  private OfficeRepository officeRepository;

  public List<Candidate> all() {
    return this.candidateRepository.findAll();
  }
  
  // Pagination of All data:
  public Map<String, Object> all(int page, int size){
    Pageable paginator = PageRequest.of(page, size, Sort.by("id").ascending());
    Page<Candidate> pageAll = this.candidateRepository.findAll(paginator);

    Map<String, Object> response = new HashMap<>();
    response.put("candidates", pageAll.getContent());
    response.put("currentPage", pageAll.getNumber());
    response.put("totalItems", pageAll.getTotalElements());
    response.put("totalPages", pageAll.getTotalPages());
    return response;
  }

  // Find a Candidate by some fields (paginated):
  public Map<String, Object> findByFilter(int page, int size, String filter){
    Pageable paginator = PageRequest.of(page, size, Sort.by("id").ascending());
    Page<Candidate> pageFiltered = this.candidateRepository
    .findCandidatesByNameContainingIgnoreCaseOrSurnameContainingIgnoreCaseOrIdNumberContainingIgnoreCaseOrPhoneContainingIgnoreCaseOrEmailContainingIgnoreCaseOrWorkerIdContainingIgnoreCase(filter, filter, filter, filter, filter, filter, paginator);

    Map<String, Object> response = new HashMap<>();
    response.put("candidates", pageFiltered.getContent());
    response.put("currentPage", pageFiltered.getNumber());
    response.put("totalItems", pageFiltered.getTotalElements());
    response.put("totalPages", pageFiltered.getTotalPages());
    return response;
  }

  public Candidate save(Candidate candidate) {
    Candidate existingCandidate = candidateRepository.findCandidateByIdNumber(candidate.getIdNumber());
    
    if (existingCandidate != null) {
      candidate = existingCandidate;
    }
    // Update proposal counter: DONE IN PROPOSAL SERVICE WHEN CREATING AND DELETING
    // candidate.setProposalsCount(candidate.getProposals().size());
    return candidateRepository.save(candidate);
  }

  public Candidate one(Long id) {
    return this.candidateRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException(id, Candidate.class));
  }

  public Candidate replace(Long id, Candidate candidate) {
    return this.candidateRepository.findById(id).map(b -> {
      if (id.equals(candidate.getId()))
        return this.candidateRepository.save(candidate);
      else
        throw new NotCouplingIdException(id, candidate.getId(), Candidate.class);
    }).orElseThrow(() -> new EntityNotFoundException(id, Candidate.class));
  }

  public void delete(Long id) {
    Set<Proposal> p = this.candidateRepository.findById(id).get().getProposals();
    Candidate c = this.candidateRepository.findById(id).get();


    if (c.getProposals() != null) {
      this.candidateRepository.findById(id).get().setProposals(null);
      for (Proposal proposal : p) {
        Office o = proposal.getOffice();
        o.getProposals().remove(proposal);
        this.officeRepository.save(o);
        proposal.setCandidate(null);
        this.proposalRepository.save(proposal);
        proposal.setOffice(null);
        //this.proposalRepository.delete(proposal);
        this.proposalRepository.deleteProposalById(proposal.getId());
      }
    }
    this.candidateRepository.delete(this.candidateRepository.findById(id).get());
  }

}
