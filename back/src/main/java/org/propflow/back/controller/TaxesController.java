package org.propflow.back.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.propflow.back.model.Taxes;
import org.propflow.back.service.TaxesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
@RestController
@RequestMapping("/v1/api/taxes")
@Tag(name = "taxes", description = "Tax brackets for salary calculations")
public class TaxesController {

  @Autowired private TaxesService taxesService;

  @Operation(summary = "List all tax brackets")
  @GetMapping(
      value = {"", "/"},
      params = {"!salary", "!maritalStatus"})
  public List<Taxes> all() {
    return this.taxesService.all();
  }

  @Operation(summary = "Find tax bracket by salary")
  @GetMapping(
      value = {"", "/"},
      params = {"salary", "!maritalStatus"})
  public Taxes findBySalary(@RequestParam Double salary) {
    return this.taxesService.findBySalary(salary);
  }

  @Operation(summary = "Find tax brackets by marital status")
  @GetMapping(
      value = {"", "/"},
      params = {"!salary", "maritalStatus"})
  public List<Taxes> findByMaritalStatus(@RequestParam Integer maritalStatus) {
    return this.taxesService.findByMaritalStatus(maritalStatus);
  }

  @Operation(summary = "Find tax bracket by salary and marital status")
  @GetMapping(
      value = {"", "/"},
      params = {"salary", "maritalStatus"})
  public Taxes findBySalaryAndMaritalStatus(
      @RequestParam Double salary, @RequestParam Integer maritalStatus) {
    return this.taxesService.findBySalaryAndMaritalStatus(salary, maritalStatus);
  }

  @Operation(summary = "Get a tax bracket by id")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Tax bracket found"),
    @ApiResponse(responseCode = "500", description = "Tax bracket not found or internal error")
  })
  @GetMapping("/{id}")
  public Taxes one(@PathVariable("id") Long id) {
    return this.taxesService.one(id);
  }

  @Operation(summary = "Create a tax bracket")
  @ApiResponse(responseCode = "200", description = "Tax bracket created")
  @PostMapping({"", "/"})
  public Taxes newTaxes(@RequestBody Taxes extra) {
    log.info("Creating new extra = " + extra);
    return this.taxesService.save(extra);
  }

  @Operation(summary = "Replace a tax bracket")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Tax bracket replaced"),
    @ApiResponse(responseCode = "500", description = "Tax bracket not found or internal error")
  })
  @PutMapping("/{id}")
  public Taxes replaceTaxes(@PathVariable("id") Long id, @RequestBody Taxes extra) {
    log.info("Updated extra with id = " + id + "\n Extra = " + extra);
    return this.taxesService.replace(id, extra);
  }

  @Operation(summary = "Delete a tax bracket")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Tax bracket deleted"),
    @ApiResponse(responseCode = "500", description = "Tax bracket not found or internal error")
  })
  @ResponseBody
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void deleteExtra(@PathVariable("id") Long id) {
    log.info("Deleting extra with id = " + id);
    this.taxesService.delete(id);
  }
}
