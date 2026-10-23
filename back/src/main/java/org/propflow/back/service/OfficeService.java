package org.propflow.back.service;

import java.util.List;
import java.util.Optional;
import org.propflow.back.exception.EntityNotFoundException;
import org.propflow.back.exception.NotCouplingIdException;
import org.propflow.back.model.Office;
import org.propflow.back.repository.OfficeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class OfficeService {

  @Autowired private OfficeRepository officeRepository;

  public List<Office> all() {
    return this.officeRepository.findAll();
  }

  public Page<Office> filter(Optional<String> wordOptional) {
    if (wordOptional.isPresent()) {
      Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));
      return officeRepository.findOfficesByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
          wordOptional.get(), wordOptional.get(), pageable);
    } else {
      return null;
    }
  }

  public Office save(Office office) {
    return this.officeRepository.save(office);
  }

  public Office one(Long id) {
    return this.officeRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException(id, Office.class));
  }

  public Office replace(Long id, Office office) {
    return this.officeRepository
        .findById(id)
        .map(
            b -> {
              if (id.equals(office.getId())) return this.officeRepository.save(office);
              else throw new NotCouplingIdException(id, office.getId(), Office.class);
            })
        .orElseThrow(() -> new EntityNotFoundException(id, Office.class));
  }

  public void delete(Long id) {
    this.officeRepository
        .findById(id)
        .map(
            b -> {
              this.officeRepository.delete(b);
              return b;
            })
        .orElseThrow(() -> new EntityNotFoundException(id, Office.class));
  }
}
