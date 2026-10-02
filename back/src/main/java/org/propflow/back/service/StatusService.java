package org.propflow.back.service;

import java.util.List;

import org.propflow.back.exception.EntityNotFoundException;
import org.propflow.back.exception.NotCouplingIdException;
import org.propflow.back.model.Status;
import org.propflow.back.repository.StatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatusService {

  @Autowired
  private StatusRepository statusRepository;

  public List<Status> all() {
    return this.statusRepository.findAll();
  }

  public Status save(Status status) {
    return this.statusRepository.save(status);
  }

  public Status one(Long id) {
    return this.statusRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException(id, Status.class));
  }

  public Status replace(Long id, Status status) {
    return this.statusRepository.findById(id).map(b -> {
      if (id.equals(status.getId()))
        return this.statusRepository.save(status);
      else
        throw new NotCouplingIdException(id, status.getId(), Status.class);
    }).orElseThrow(() -> new EntityNotFoundException(id, Status.class));
  }

  public void delete(Long id) {
    this.statusRepository.findById(id).map(b -> {
      this.statusRepository.delete(b);
      return b;
    })
        .orElseThrow(() -> new EntityNotFoundException(id, Status.class));
  }

}
