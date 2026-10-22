package org.propflow.back.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.propflow.back.dto.ProposalDTO;
import org.propflow.back.mapper.ProposalMapper;
import org.propflow.back.model.Proposal;
import org.propflow.back.service.ProposalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/v1/api/proposals")
@Tag(name = "proposals", description = "Proposal management")
public class ProposalController {

  @Autowired private ProposalMapper proposalMapper;
  @Autowired private ProposalService proposalService;

  @Operation(summary = "List all proposals")
  @GetMapping({"", "/"})
  public List<ProposalDTO> all() {
    return proposalService.all();
  }

  // Get all proposals paginated
  @Operation(summary = "List proposals paginated")
  @GetMapping(
      value = {"", "/"},
      params = {"page", "size", "!filter", "!candidate"})
  public ResponseEntity<Map<String, Object>> allPaginated(
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "3") int size) {
    log.info("Accessing paginated Proposals");
    log.info("PAGE: " + page + " & SIZE: " + size);

    Map<String, Object> responseAll = this.proposalService.all(page, size);
    return ResponseEntity.ok(responseAll);
  }

  // Get all proposals filtered and paginated
  @Operation(summary = "List proposals paginated and filtered")
  @GetMapping(
      value = {"", "/"},
      params = {"page", "size", "filter"})
  public ResponseEntity<Map<String, Object>> allFiltered(
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "3") int size,
      @RequestParam String filter) {
    log.info("Accessing paginated and filtered Proposals");
    log.info("PAGE: " + page + " & SIZE: " + size + " & filtered by: " + filter);

    Map<String, Object> responseAll = this.proposalService.findByFilter(page, size, filter);
    return ResponseEntity.ok(responseAll);
  }

  // Get proposals by candidate ID
  @Operation(summary = "List proposals by candidate id")
  @GetMapping(
      value = {"", "/"},
      params = {"candidate", "!page", "!size", "!filter"})
  public List<ProposalDTO> allByCandidateId(@RequestParam(value = "candidate") long candidate) {
    log.info("Accessing paginated Proposals using Candidate ID " + candidate);

    return this.proposalService.findProposalsByCandidateId(candidate);
  }

  @Operation(summary = "Find proposals by candidate name")
  @GetMapping(
      value = {"", "/"},
      params = {"!page", "!size", "filter"})
  public List<Proposal> findByCandidate(@RequestParam String filter) {
    return proposalService.findByCandidate(filter);
  }

  @Operation(summary = "Get a proposal by id")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Proposal found"),
    @ApiResponse(responseCode = "404", description = "Proposal not found")
  })
  @GetMapping("/{id}")
  public ProposalDTO one(@PathVariable("id") Long id) {
    Proposal p = proposalService.one(id);
    System.out.println(p.getRole());
    return proposalMapper.toDTO(p);
  }

  @Operation(summary = "Create a proposal")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Proposal created"),
    @ApiResponse(responseCode = "400", description = "Invalid proposal payload")
  })
  @PostMapping({"", "/"})
  public Proposal newProposal(@Valid @RequestBody Proposal proposal) {
    // log.info("Creating new proposal = " + proposal);
    System.out.println(proposal.getNjoyn());
    return this.proposalService.save(proposal, false);
  }

  @Operation(summary = "Replace a proposal")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Proposal replaced"),
    @ApiResponse(responseCode = "400", description = "Invalid proposal payload"),
    @ApiResponse(responseCode = "404", description = "Proposal not found")
  })
  @PutMapping("/{id}")
  public Proposal replaceProposal(
      @PathVariable("id") Long id, @Valid @RequestBody Proposal proposal) {
    // log.info("Updated proposal with id = " + id + "\n proposal = " + proposal);
    return this.proposalService.replace(id, proposal);
  }

  @Operation(summary = "Delete a proposal")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Proposal deleted"),
    @ApiResponse(responseCode = "404", description = "Proposal not found")
  })
  @ResponseBody
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void deleteProposal(@PathVariable("id") Long id) {
    log.info("Deleting proposal with id = " + id);
    this.proposalService.delete(id);
  }
}
