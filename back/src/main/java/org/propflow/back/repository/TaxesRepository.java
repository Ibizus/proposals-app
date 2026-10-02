package org.propflow.back.repository;

import java.util.List;

import org.propflow.back.model.Taxes;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaxesRepository extends JpaRepository<Taxes, Long> {

  public Taxes findFirstBySalaryMaxGreaterThanEqualOrderBySalaryMaxAsc(double salary);

  public Taxes findFirstBySalaryMaxGreaterThanEqualAndMaritalStatusOrderBySalaryMaxAsc(double salary,
      int maritalStatus);

  public List<Taxes> findByMaritalStatusOrderBySalaryMaxAsc(int maritalStatus);
}
