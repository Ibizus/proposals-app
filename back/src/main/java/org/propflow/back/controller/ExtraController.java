package org.propflow.back.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.propflow.back.model.Extra;
import org.propflow.back.service.ExtraService;
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
@RequestMapping("/v1/api/extras")
@Tag(name = "extras", description = "Salary extras")
public class ExtraController {

  @Autowired private ExtraService extraService;

  @Operation(summary = "List all extras")
  @GetMapping({"", "/"})
  public List<Extra> all() {
    return this.extraService.all();
  }

  @Operation(summary = "Find extras by name or description")
  @GetMapping(
      value = {"", "/"},
      params = "filter")
  public List<Extra> filterByNameAndDescription(@RequestParam String filter) {
    return this.extraService.findByNameAndDescription(filter);
  }

  @Operation(summary = "Get an extra by id")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Extra found"),
    @ApiResponse(responseCode = "404", description = "Extra not found")
  })
  @GetMapping("/{id}")
  public Extra one(@PathVariable("id") Long id) {
    return this.extraService.one(id);
  }

  @Operation(summary = "Create an extra")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Extra created"),
    @ApiResponse(responseCode = "400", description = "Invalid extra payload")
  })
  @PostMapping({"", "/"})
  public Extra newExtra(@Valid @RequestBody Extra extra) {
    log.info("Creating new extra = " + extra);
    return this.extraService.save(extra);
  }

  @Operation(summary = "Replace an extra")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Extra replaced"),
    @ApiResponse(responseCode = "400", description = "Invalid extra payload"),
    @ApiResponse(responseCode = "404", description = "Extra not found"),
    @ApiResponse(responseCode = "409", description = "Path id does not match extra id")
  })
  @PutMapping("/{id}")
  public Extra replaceExtra(@PathVariable("id") Long id, @Valid @RequestBody Extra extra) {
    log.info("Updated extra with id = " + id + "\n Extra = " + extra);
    return this.extraService.replace(id, extra);
  }

  @Operation(summary = "Delete an extra")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Extra deleted"),
    @ApiResponse(responseCode = "404", description = "Extra not found")
  })
  @ResponseBody
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void deleteExtra(@PathVariable("id") Long id) {
    log.info("Deleting extra with id = " + id);
    this.extraService.delete(id);
  }
}
