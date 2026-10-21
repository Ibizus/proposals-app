package org.propflow.back.controller;

import jakarta.validation.Valid;
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
public class TaxesController {

  @Autowired private TaxesService taxesService;

  @GetMapping(
      value = {"", "/"},
      params = {"!salary", "!maritalStatus"})
  public List<Taxes> all() {
    return this.taxesService.all();
  }

  @GetMapping(
      value = {"", "/"},
      params = {"salary", "!maritalStatus"})
  public Taxes findBySalary(@RequestParam Double salary) {
    return this.taxesService.findBySalary(salary);
  }

  @GetMapping(
      value = {"", "/"},
      params = {"!salary", "maritalStatus"})
  public List<Taxes> findByMaritalStatus(@RequestParam Integer maritalStatus) {
    return this.taxesService.findByMaritalStatus(maritalStatus);
  }

  @GetMapping(
      value = {"", "/"},
      params = {"salary", "maritalStatus"})
  public Taxes findBySalaryAndMaritalStatus(
      @RequestParam Double salary, @RequestParam Integer maritalStatus) {
    return this.taxesService.findBySalaryAndMaritalStatus(salary, maritalStatus);
  }

  @GetMapping("/{id}")
  public Taxes one(@PathVariable("id") Long id) {
    return this.taxesService.one(id);
  }

  @PostMapping({"", "/"})
  public Taxes newTaxes(@Valid @RequestBody Taxes extra) {
    log.info("Creating new extra = " + extra);
    return this.taxesService.save(extra);
  }

  @PutMapping("/{id}")
  public Taxes replaceTaxes(@PathVariable("id") Long id, @Valid @RequestBody Taxes extra) {
    log.info("Updated extra with id = " + id + "\n Extra = " + extra);
    return this.taxesService.replace(id, extra);
  }

  @ResponseBody
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void deleteExtra(@PathVariable("id") Long id) {
    log.info("Deleting extra with id = " + id);
    this.taxesService.delete(id);
  }
}
