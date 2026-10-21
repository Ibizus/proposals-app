package org.propflow.back.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.propflow.back.model.Parameter;
import org.propflow.back.service.ParameterService;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
@RestController
@RequestMapping("/v1/api/parameters")
public class ParameterController {

  @Autowired private ParameterService ParameterService;

  @GetMapping({"", "/"})
  public List<Parameter> all() {
    return this.ParameterService.all();
  }

  @GetMapping("/{id}")
  public Parameter one(@PathVariable("id") Long id) {
    return this.ParameterService.one(id);
  }

  @PostMapping({"", "/"})
  public Parameter newParameter(@Valid @RequestBody Parameter Parameter) {
    log.info("Creating new Parameter = " + Parameter);
    return this.ParameterService.save(Parameter);
  }

  @PutMapping("/{id}")
  public Parameter replaceParameter(
      @PathVariable("id") Long id, @Valid @RequestBody Parameter Parameter) {
    log.info("Updated Parameter with id = " + id + "\n Parameter = " + Parameter);
    return this.ParameterService.replace(id, Parameter);
  }

  @ResponseBody
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void deleteParameter(@PathVariable("id") Long id) {
    log.info("Deleting Parameter with id = " + id);
    this.ParameterService.delete(id);
  }
}
