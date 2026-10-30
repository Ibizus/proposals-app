package org.propflow.back.repository;

import java.util.Optional;
import org.propflow.back.model.TaxConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaxConfigurationRepository extends JpaRepository<TaxConfiguration, Long> {

  Optional<TaxConfiguration> findByActiveTrue();

  Optional<TaxConfiguration> findByCountryCode(String countryCode);
}
