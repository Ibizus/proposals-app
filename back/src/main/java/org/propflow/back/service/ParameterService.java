package org.propflow.back.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.propflow.back.exception.EntityNotFoundException;
import org.propflow.back.exception.NotCouplingIdException;
import org.propflow.back.model.Parameter;
import org.propflow.back.repository.ParameterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ParameterService {

  @Autowired private ParameterRepository parameterRepository;

  public List<Parameter> all() {
    return this.parameterRepository.findAll();
  }

  // Get specific parameters for Salary calculations: (IHT, ISS, SPP)
  public Map<String, Double> getCalculationsParameters() {
    return parameterRepository.findAll().stream()
        .filter(
            p ->
                p.getName().equals("iht") || p.getName().equals("spp") || p.getName().equals("scr"))
        .collect(Collectors.toMap(Parameter::getName, Parameter::getValue));
  }

  public Parameter save(Parameter Parameter) {
    return this.parameterRepository.save(Parameter);
  }

  public Parameter one(Long id) {
    return this.parameterRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException(id, Parameter.class));
  }

  public Parameter replace(Long id, Parameter Parameter) {
    return this.parameterRepository
        .findById(id)
        .map(
            b -> {
              if (id.equals(Parameter.getId())) return this.parameterRepository.save(Parameter);
              else throw new NotCouplingIdException(id, Parameter.getId(), Parameter.class);
            })
        .orElseThrow(() -> new EntityNotFoundException(id, Parameter.class));
  }

  public void delete(Long id) {
    this.parameterRepository
        .findById(id)
        .map(
            b -> {
              this.parameterRepository.delete(b);
              return b;
            })
        .orElseThrow(() -> new EntityNotFoundException(id, Parameter.class));
  }
}
