package org.propflow.back.repository;

import org.propflow.back.model.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {

  @Modifying
  @Query("update Candidate c set c.position = null where c.position = :position")
  void unlinkCandidates(@Param("position") Position position);

  @Modifying
  @Query("update Proposal p set p.position = null where p.position = :position")
  void unlinkProposals(@Param("position") Position position);
}
