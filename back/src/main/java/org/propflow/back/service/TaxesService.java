package org.propflow.back.service;

import java.util.List;
import org.propflow.back.exception.EntityNotFoundException;
import org.propflow.back.exception.NotCouplingIdException;
import org.propflow.back.model.Taxes;
import org.propflow.back.repository.TaxesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaxesService {

  @Autowired private TaxesRepository taxesRepository;

  public List<Taxes> all() {
    return this.taxesRepository.findAll();
  }

  public Taxes findBySalary(Double salary) {
    return this.taxesRepository.findFirstBySalaryMaxGreaterThanEqualOrderBySalaryMaxAsc(salary);
  }

  public List<Taxes> findByMaritalStatus(Integer maritalStatus) {
    return this.taxesRepository.findByMaritalStatusOrderBySalaryMaxAsc(maritalStatus);
  }

  public Taxes findBySalaryAndMaritalStatus(Double salary, int maritalStatus) {
    return this.taxesRepository
        .findFirstBySalaryMaxGreaterThanEqualAndMaritalStatusOrderBySalaryMaxAsc(
            salary, maritalStatus);
  }

  public Taxes save(Taxes extra) {
    return this.taxesRepository.save(extra);
  }

  public Taxes one(Long id) {
    return this.taxesRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException(id, Taxes.class));
  }

  public Taxes replace(Long id, Taxes extra) {
    return this.taxesRepository
        .findById(id)
        .map(
            b -> {
              if (id.equals(extra.getId())) return this.taxesRepository.save(extra);
              else throw new NotCouplingIdException(id, extra.getId(), Taxes.class);
            })
        .orElseThrow(() -> new EntityNotFoundException(id, Taxes.class));
  }

  public void delete(Long id) {
    this.taxesRepository
        .findById(id)
        .map(
            b -> {
              this.taxesRepository.delete(b);
              return b;
            })
        .orElseThrow(() -> new EntityNotFoundException(id, Taxes.class));
  }

  public double getIrsByNumberOfDependants(Taxes taxBracket, Integer dependentsNumber) {
    double irs = 0d;

    if (dependentsNumber <= 0) {
      irs = taxBracket.getZero();
    } else if (dependentsNumber == 1) {
      irs = taxBracket.getOne();
    } else if (dependentsNumber == 2) {
      irs = taxBracket.getTwo();
    } else if (dependentsNumber == 3) {
      irs = taxBracket.getThree();
    } else if (dependentsNumber == 4) {
      irs = taxBracket.getFour();
    } else {
      irs = taxBracket.getFive();
    }
    return irs;
  }
}
