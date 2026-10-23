package org.propflow.back.repository;

import jakarta.transaction.Transactional;
import java.util.List;
import org.propflow.back.model.Proposal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProposalRepository extends JpaRepository<Proposal, Long> {

  // Find by Candidate Name, Surname & Email
  public List<Proposal>
      findProposalsByCandidate_NameContainingIgnoreCaseOrCandidate_SurnameContainingIgnoreCaseOrCandidate_EmailContainingIgnoreCase(
          String name, String surname, String email);

  @Transactional
  @Modifying
  @Query(value = "DELETE FROM proposal WHERE id = :id", nativeQuery = true)
  void deleteProposalById(@Param("id") Long id);

  // Find Proposal by various fields:
  public Page<Proposal>
      findProposalByNjoynOrDescriptionContainingIgnoreCaseOrRoleContainingIgnoreCaseOrCandidate_NameContainingIgnoreCaseOrCandidate_SurnameContainingIgnoreCaseOrCandidate_EmailContainingIgnoreCase(
          String njoyn,
          String desription,
          String role,
          String candidateName,
          String candidateSurname,
          String candidateEmail,
          Pageable pageable);
}
