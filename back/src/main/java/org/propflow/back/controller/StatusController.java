package org.propflow.back.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.propflow.back.model.Status;
import org.propflow.back.service.StatusService;
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
@RequestMapping("/v1/api/status")
@Tag(name = "status", description = "Proposal status history")
public class StatusController {

  @Autowired private StatusService statusService;

  @Operation(summary = "List all statuses")
  @GetMapping(
      value = {"", "/"},
      params = {"!salary", "!maritalStatus"})
  public List<Status> all() {
    return this.statusService.all();
  }

  @Operation(summary = "Get a status by id")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Status found"),
    @ApiResponse(responseCode = "404", description = "Status not found")
  })
  @GetMapping("/{id}")
  public Status one(@PathVariable("id") Long id) {
    return this.statusService.one(id);
  }

  @Operation(summary = "Create a status")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Status created"),
    @ApiResponse(responseCode = "400", description = "Invalid status payload")
  })
  @PostMapping({"", "/"})
  public Status newStatus(@Valid @RequestBody Status extra) {
    log.info("Creating new extra = " + extra);
    return this.statusService.save(extra);
  }

  @Operation(summary = "Replace a status")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Status replaced"),
    @ApiResponse(responseCode = "400", description = "Invalid status payload"),
    @ApiResponse(responseCode = "404", description = "Status not found"),
    @ApiResponse(responseCode = "409", description = "Path id does not match status id")
  })
  @PutMapping("/{id}")
  public Status replaceStatus(@PathVariable("id") Long id, @Valid @RequestBody Status extra) {
    log.info("Updated extra with id = " + id + "\n Extra = " + extra);
    return this.statusService.replace(id, extra);
  }

  @Operation(summary = "Delete a status")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Status deleted"),
    @ApiResponse(responseCode = "404", description = "Status not found")
  })
  @ResponseBody
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void deleteExtra(@PathVariable("id") Long id) {
    log.info("Deleting extra with id = " + id);
    this.statusService.delete(id);
  }
}
