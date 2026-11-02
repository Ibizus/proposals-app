package org.propflow.back.service;

import java.util.List;
import org.propflow.back.exception.EntityNotFoundException;
import org.propflow.back.model.Tax;
import org.propflow.back.model.TaxBracket;
import org.propflow.back.model.TaxConfiguration;
import org.propflow.back.model.TaxType;
import org.propflow.back.repository.TaxConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaxConfigurationService {

  @Autowired private TaxConfigurationRepository taxConfigurationRepository;

  public List<TaxConfiguration> all() {
    return taxConfigurationRepository.findAll();
  }

  public TaxConfiguration findActive() {
    return taxConfigurationRepository
        .findByActiveTrue()
        .orElseThrow(() -> new IllegalStateException("No active tax configuration"));
  }

  @Transactional
  public TaxConfiguration save(TaxConfiguration configuration) {
    configuration.setActive(Boolean.TRUE.equals(configuration.getActive()));
    wireChildren(configuration);
    if (configuration.getActive()) {
      deactivateOthers(configuration.getId());
    }
    return taxConfigurationRepository.save(configuration);
  }

  @Transactional
  public TaxConfiguration activate(Long id) {
    TaxConfiguration target =
        taxConfigurationRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(id, TaxConfiguration.class));
    for (TaxConfiguration config : taxConfigurationRepository.findAll()) {
      config.setActive(false);
    }
    target.setActive(true);
    return target;
  }

  public TaxConfiguration one(Long id) {
    return taxConfigurationRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException(id, TaxConfiguration.class));
  }

  private void deactivateOthers(Long keptId) {
    for (TaxConfiguration config : taxConfigurationRepository.findAll()) {
      if (keptId == null || config.getId() != keptId) {
        config.setActive(false);
      }
    }
  }

  private void wireChildren(TaxConfiguration configuration) {
    if (configuration.getTaxes() != null) {
      for (Tax tax : configuration.getTaxes()) {
        tax.setConfiguration(configuration);
        if (tax.getType() == TaxType.FIXED) {
          tax.setBrackets(null);
        } else {
          tax.setFixedRate(null);
          if (tax.getBrackets() != null) {
            for (TaxBracket bracket : tax.getBrackets()) {
              bracket.setTax(tax);
            }
          }
        }
      }
    }
  }
}
