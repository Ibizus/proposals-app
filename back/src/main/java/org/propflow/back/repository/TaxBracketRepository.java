package org.propflow.back.repository;

import java.util.List;
import org.propflow.back.model.TaxBracket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaxBracketRepository extends JpaRepository<TaxBracket, Long> {

  List<TaxBracket> findByTaxIdOrderByThresholdAsc(long taxId);
}
