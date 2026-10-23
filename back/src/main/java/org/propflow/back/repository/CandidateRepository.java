package org.propflow.back.repository;

import org.propflow.back.model.Candidate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {

  public Candidate findCandidateByIdNumber(String idNumber);

  public Page<Candidate>
      findCandidatesByNameContainingIgnoreCaseOrSurnameContainingIgnoreCaseOrIdNumberContainingIgnoreCaseOrPhoneContainingIgnoreCaseOrEmailContainingIgnoreCaseOrWorkerIdContainingIgnoreCase(
          String name,
          String surname,
          String id,
          String phone,
          String email,
          String workerId,
          Pageable pageable);
}
