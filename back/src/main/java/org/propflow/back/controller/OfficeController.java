package org.propflow.back.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "offices", description = "Office management")
public class OfficeController {

  @Autowired private OfficeService officeService;

  @Operation(summary = "List all offices")
  @GetMapping({"", "/"})
  public List<Office> all() {
    return this.officeService.all();
  }

  // Filters by Name and Code
  @Operation(summary = "Find offices by name or code")
  @GetMapping(
      value = {"", "/"},
      params = "filter")
  public List<Office> findOfficeByNameAndCode(
      @RequestParam("filter") Optional<String> filterOptional) {
    return this.officeService.filter(filterOptional).getContent();
  }

  @Operation(summary = "Get an office by id")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Office found"),
    @ApiResponse(responseCode = "500", description = "Office not found or internal error")
  })
  @GetMapping("/{id}")
  public Office one(@PathVariable("id") Long id) {
    return this.officeService.one(id);
  }

  @Operation(summary = "Create an office")
  @ApiResponse(responseCode = "200", description = "Office created")
  @PostMapping({"", "/"})
  public Office newOffice(@RequestBody Office office) {
    return this.officeService.save(office);
  }

  @Operation(summary = "Replace an office")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Office replaced"),
    @ApiResponse(responseCode = "500", description = "Office not found or internal error")
  })
  @PutMapping("/{id}")
  public Office replaceOffice(@PathVariable("id") Long id, @RequestBody Office office) {
    return this.officeService.replace(id, office);
  }

  @Operation(summary = "Delete an office")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Office deleted"),
    @ApiResponse(responseCode = "500", description = "Office not found or internal error")
  })
  @ResponseBody
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void deleteOffice(@PathVariable("id") Long id) {
    this.officeService.delete(id);
  }
}
