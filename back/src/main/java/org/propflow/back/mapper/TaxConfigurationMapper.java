package org.propflow.back.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.propflow.back.dto.TaxBracketDTO;
import org.propflow.back.dto.TaxConfigurationDTO;
import org.propflow.back.dto.TaxDTO;
import org.propflow.back.model.Tax;
import org.propflow.back.model.TaxBracket;
import org.propflow.back.model.TaxConfiguration;
import org.propflow.back.model.TaxType;
import org.springframework.stereotype.Component;

/** Maps between {@link TaxConfiguration} and {@link TaxConfigurationDTO}. */
@Component
public class TaxConfigurationMapper {

  public TaxConfigurationDTO toDTO(TaxConfiguration config) {
    return new TaxConfigurationDTO(
        config.getId(),
        config.getCountryCode(),
        config.getLabel(),
        config.getYearlyPayments(),
        config.getActive(),
        config.getTaxes().stream().map(this::toDTO).collect(Collectors.toList()));
  }

  public TaxDTO toDTO(Tax tax) {
    return new TaxDTO(
        tax.getId(),
        tax.getName(),
        tax.getType(),
        tax.getFixedRate(),
        tax.getExtrasOnly(),
        tax.getBrackets() == null
            ? List.of()
            : tax.getBrackets().stream().map(this::toDTO).collect(Collectors.toList()));
  }

  public TaxBracketDTO toDTO(TaxBracket bracket) {
    return new TaxBracketDTO(bracket.getId(), bracket.getThreshold(), bracket.getRate());
  }

  public TaxConfiguration toEntity(TaxConfigurationDTO dto) {
    TaxConfiguration config = new TaxConfiguration();
    config.setId(dto.id() == null ? 0 : dto.id());
    config.setCountryCode(dto.countryCode());
    config.setLabel(dto.label());
    config.setYearlyPayments(dto.yearlyPayments());
    config.setActive(dto.active());
    config.setTaxes(
        dto.taxes() == null
            ? new ArrayList<>()
            : dto.taxes().stream().map(t -> toEntity(t, config)).collect(Collectors.toList()));
    return config;
  }

  public Tax toEntity(TaxDTO dto, TaxConfiguration config) {
    Tax tax = new Tax();
    tax.setId(dto.id() == null ? 0 : dto.id());
    tax.setName(dto.name());
    tax.setType(dto.type());
    tax.setExtrasOnly(dto.extrasOnly());
    tax.setConfiguration(config);
    if (dto.type() == TaxType.FIXED) {
      tax.setFixedRate(dto.fixedRate());
      tax.setBrackets(new ArrayList<>());
    } else {
      tax.setFixedRate(null);
      tax.setBrackets(
          dto.brackets() == null
              ? new ArrayList<>()
              : dto.brackets().stream().map(b -> toEntity(b, tax)).collect(Collectors.toList()));
    }
    return tax;
  }

  public TaxBracket toEntity(TaxBracketDTO dto, Tax tax) {
    TaxBracket bracket = new TaxBracket();
    bracket.setId(dto.id() == null ? 0 : dto.id());
    bracket.setThreshold(dto.threshold());
    bracket.setRate(dto.rate());
    bracket.setTax(tax);
    return bracket;
  }
}
