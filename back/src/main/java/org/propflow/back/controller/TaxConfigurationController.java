package org.propflow.back.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.propflow.back.dto.TaxConfigurationDTO;
import org.propflow.back.mapper.TaxConfigurationMapper;
import org.propflow.back.service.TaxConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
@RestController
@RequestMapping("/v1/api/tax-configurations")
@Tag(name = "tax-configurations", description = "Configurable country-specific tax models")
public class TaxConfigurationController {

  @Autowired private TaxConfigurationService taxConfigurationService;
  @Autowired private TaxConfigurationMapper taxConfigurationMapper;

  @Operation(summary = "List all tax configurations")
  @GetMapping({"", "/"})
  public List<TaxConfigurationDTO> all() {
    return taxConfigurationService.all().stream().map(taxConfigurationMapper::toDTO).toList();
  }

  @Operation(summary = "Get the active tax configuration")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Active configuration found"),
    @ApiResponse(responseCode = "404", description = "No active configuration")
  })
  @GetMapping("/active")
  public TaxConfigurationDTO active() {
    return taxConfigurationMapper.toDTO(taxConfigurationService.findActive());
  }

  @Operation(summary = "Get a tax configuration by id")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Configuration found"),
    @ApiResponse(responseCode = "404", description = "Configuration not found")
  })
  @GetMapping("/{id}")
  public TaxConfigurationDTO one(@PathVariable("id") Long id) {
    return taxConfigurationMapper.toDTO(taxConfigurationService.one(id));
  }

  @Operation(summary = "Create a tax configuration")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Configuration created"),
    @ApiResponse(responseCode = "400", description = "Invalid configuration payload")
  })
  @PostMapping({"", "/"})
  public TaxConfigurationDTO create(@Valid @RequestBody TaxConfigurationDTO dto) {
    log.info("Creating tax configuration for country = {}", dto.countryCode());
    return taxConfigurationMapper.toDTO(
        taxConfigurationService.save(taxConfigurationMapper.toEntity(dto)));
  }

  @Operation(summary = "Activate a tax configuration")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Configuration activated"),
    @ApiResponse(responseCode = "404", description = "Configuration not found")
  })
  @PostMapping("/{id}/activate")
  public TaxConfigurationDTO activate(@PathVariable("id") Long id) {
    log.info("Activating tax configuration with id = {}", id);
    return taxConfigurationMapper.toDTO(taxConfigurationService.activate(id));
  }
}
