package org.propflow.back.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import org.propflow.back.model.Office;
import org.propflow.back.service.OfficeService;
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

// import lombok.extern.slf4j.Slf4j;
// @Slf4j
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
@RestController
@RequestMapping("/v1/api/offices")
public class OfficeController {

  @Autowired private OfficeService officeService;

  @GetMapping({"", "/"})
  public List<Office> all() {
    return this.officeService.all();
  }

  // Filters by Name and Code
  @GetMapping(
      value = {"", "/"},
      params = "filter")
  public List<Office> findOfficeByNameAndCode(
      @RequestParam("filter") Optional<String> filterOptional) {
    return this.officeService.filter(filterOptional).getContent();
  }

  @GetMapping("/{id}")
  public Office one(@PathVariable("id") Long id) {
    return this.officeService.one(id);
  }

  @PostMapping({"", "/"})
  public Office newOffice(@Valid @RequestBody Office office) {
    return this.officeService.save(office);
  }

  @PutMapping("/{id}")
  public Office replaceOffice(@PathVariable("id") Long id, @Valid @RequestBody Office office) {
    return this.officeService.replace(id, office);
  }

  @ResponseBody
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void deleteOffice(@PathVariable("id") Long id) {
    this.officeService.delete(id);
  }
}
