package org.propflow.back.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.propflow.back.model.Salary;
import org.propflow.back.service.SalaryService;
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
@RequestMapping("/v1/api/salaries")
@Tag(name = "salaries", description = "Proposal salaries")
public class SalaryController {

  @Autowired private SalaryService salaryService;

  @Operation(summary = "List all salaries")
  @GetMapping({"", "/"})
  public List<Salary> all() {
    return salaryService.all();
  }

  @Operation(summary = "Get a salary by id")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Salary found"),
    @ApiResponse(responseCode = "404", description = "Salary not found")
  })
  @GetMapping("/{id}")
  public Salary one(@PathVariable("id") Long id) {
    return salaryService.one(id);
  }

  @Operation(summary = "Create a salary")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Salary created"),
    @ApiResponse(responseCode = "400", description = "Invalid salary payload")
  })
  @PostMapping({"", "/"})
  public Salary newSalary(@Valid @RequestBody Salary salary) {
    log.info("Creating new salary = " + salary);
    return this.salaryService.save(salary);
  }

  @Operation(summary = "Replace a salary")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Salary replaced"),
    @ApiResponse(responseCode = "400", description = "Invalid salary payload"),
    @ApiResponse(responseCode = "404", description = "Salary not found"),
    @ApiResponse(responseCode = "409", description = "Path id does not match salary id")
  })
  @PutMapping("/{id}")
  public Salary replaceSalary(@PathVariable("id") Long id, @Valid @RequestBody Salary salary) {
    log.info("Updated salary with id = " + id + "\n Salary = " + salary);
    return this.salaryService.replace(id, salary);
  }

  @Operation(summary = "Delete a salary")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Salary deleted"),
    @ApiResponse(responseCode = "404", description = "Salary not found")
  })
  @ResponseBody
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void deleteSalary(@PathVariable("id") Long id) {
    log.info("Deleting salary with id = " + id);
    this.salaryService.delete(id);
  }
}
