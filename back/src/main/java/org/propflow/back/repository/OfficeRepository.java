package org.propflow.back.repository;

import org.propflow.back.model.Office;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OfficeRepository extends JpaRepository<Office, Long> {

  // Find Offices By Name or Code
  public Page<Office> findOfficesByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
      String name, String code, Pageable pageable);
}
