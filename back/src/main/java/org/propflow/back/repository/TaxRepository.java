package org.propflow.back.repository;

import java.util.List;
import org.propflow.back.model.Tax;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaxRepository extends JpaRepository<Tax, Long> {

  List<Tax> findByConfigurationId(long configurationId);
}
