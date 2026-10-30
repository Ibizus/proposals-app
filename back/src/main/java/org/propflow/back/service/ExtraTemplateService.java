package org.propflow.back.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.propflow.back.exception.EntityNotFoundException;
import org.propflow.back.exception.NotCouplingIdException;
import org.propflow.back.model.ExtraTemplate;
import org.propflow.back.model.TaxConfiguration;
import org.propflow.back.repository.ExtraTemplateRepository;
import org.propflow.back.repository.TaxConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ExtraTemplateService {

  @Autowired private ExtraTemplateRepository extraTemplateRepository;
  @Autowired private TaxConfigurationRepository taxConfigurationRepository;

  public List<ExtraTemplate> all() {
    return all(null);
  }

  public List<ExtraTemplate> all(Long taxConfigurationId) {
    Long resolvedId = resolveConfigurationId(taxConfigurationId);
    return resolvedId == null
        ? this.extraTemplateRepository.findAll()
        : this.extraTemplateRepository.findByTaxConfigurationId(resolvedId);
  }

  // Pagination:
  public Map<String, Object> all(int page, int size) {
    return all(page, size, null);
  }

  public Map<String, Object> all(int page, int size, Long taxConfigurationId) {
    Long resolvedId = resolveConfigurationId(taxConfigurationId);
    Pageable paginator = PageRequest.of(page, size, Sort.by("id").ascending());
    Page<ExtraTemplate> pageAll =
        resolvedId == null
            ? this.extraTemplateRepository.findAll(paginator)
            : this.extraTemplateRepository.findByTaxConfigurationId(resolvedId, paginator);

    Map<String, Object> response = new HashMap<>();

    response.put("extras", pageAll.getContent());
    response.put("currentPage", pageAll.getNumber());
    response.put("totalItems", pageAll.getTotalElements());
    response.put("totalPages", pageAll.getTotalPages());
    return response;
  }

  private Long resolveConfigurationId(Long taxConfigurationId) {
    if (taxConfigurationId != null) {
      return taxConfigurationId;
    }
    return taxConfigurationRepository.findByActiveTrue().map(TaxConfiguration::getId).orElse(null);
  }

  public List<ExtraTemplate> findByName(String input) {
    return extraTemplateRepository.findExtraTemplatesByNameContainingIgnoreCase(input);
  }

  public List<ExtraTemplate> findByName(String input, Long taxConfigurationId) {
    return extraTemplateRepository.findExtraTemplatesByNameContainingIgnoreCase(input).stream()
        .filter(
            t -> taxConfigurationId == null || taxConfigurationId.equals(t.getTaxConfigurationId()))
        .toList();
  }

  public ExtraTemplate save(ExtraTemplate extra) {
    if (extra.getTaxConfigurationId() == null) {
      extra.setTaxConfigurationId(
          taxConfigurationRepository.findByActiveTrue().map(TaxConfiguration::getId).orElse(null));
    }
    return this.extraTemplateRepository.save(extra);
  }

  public ExtraTemplate one(Long id) {
    return this.extraTemplateRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException(id, ExtraTemplate.class));
  }

  public ExtraTemplate replace(Long id, ExtraTemplate extra) {
    return this.extraTemplateRepository
        .findById(id)
        .map(
            b -> {
              if (id.equals(extra.getId())) return this.extraTemplateRepository.save(extra);
              else throw new NotCouplingIdException(id, extra.getId(), ExtraTemplate.class);
            })
        .orElseThrow(() -> new EntityNotFoundException(id, ExtraTemplate.class));
  }

  public void delete(Long id) {
    this.extraTemplateRepository
        .findById(id)
        .map(
            b -> {
              this.extraTemplateRepository.delete(b);
              return b;
            })
        .orElseThrow(() -> new EntityNotFoundException(id, ExtraTemplate.class));
  }
}
