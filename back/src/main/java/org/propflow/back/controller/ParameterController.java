package org.propflow.back.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "parameters", description = "Salary parameters")
public class ParameterController {

  @Autowired private ParameterService ParameterService;

  @Operation(summary = "List all parameters")
  @GetMapping({"", "/"})
  public List<Parameter> all() {
    return this.ParameterService.all();
  }

  @Operation(summary = "Get a parameter by id")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Parameter found"),
    @ApiResponse(responseCode = "500", description = "Parameter not found or internal error")
  })
  @GetMapping("/{id}")
  public Parameter one(@PathVariable("id") Long id) {
    return this.ParameterService.one(id);
  }

  @Operation(summary = "Create a parameter")
  @ApiResponse(responseCode = "200", description = "Parameter created")
  @PostMapping({"", "/"})
  public Parameter newParameter(@RequestBody Parameter Parameter) {
    log.info("Creating new Parameter = " + Parameter);
    return this.ParameterService.save(Parameter);
  }

  @Operation(summary = "Replace a parameter")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Parameter replaced"),
    @ApiResponse(responseCode = "500", description = "Parameter not found or internal error")
  })
  @PutMapping("/{id}")
  public Parameter replaceParameter(@PathVariable("id") Long id, @RequestBody Parameter Parameter) {
    log.info("Updated Parameter with id = " + id + "\n Parameter = " + Parameter);
    return this.ParameterService.replace(id, Parameter);
  }

  @Operation(summary = "Delete a parameter")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Parameter deleted"),
    @ApiResponse(responseCode = "500", description = "Parameter not found or internal error")
  })
  @ResponseBody
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void deleteParameter(@PathVariable("id") Long id) {
    log.info("Deleting Parameter with id = " + id);
    this.ParameterService.delete(id);
  }
}
