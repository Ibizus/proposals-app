package org.propflow.back.controller;

import java.util.List;
import java.util.Map;
import org.propflow.back.model.Candidate;
import org.propflow.back.service.CandidateService;
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@CrossOrigin(origins = { "http://localhost:4200", "http://localhost:3000" })
@RestController
@RequestMapping("/v1/api/candidates")
public class CandidateController {

  @Autowired
  private CandidateService candidateService;

  @GetMapping(value = { "", "/" })
  public List<Candidate> all() {
    log.info("Accessing All Candidates");
    return candidateService.all();
  }

  @GetMapping(value = { "", "/" }, params = { "page", "size", "!filter" })
  public ResponseEntity<Map<String, Object>> all(
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "3") int size) {
    log.info("Accessing paginated Candidates");
    log.info("PAGE: " + page + " & SIZE: " + size);

    Map<String, Object> responseAll = this.candidateService.all(page, size);
    return ResponseEntity.ok(responseAll);
  }

  @GetMapping(value = { "", "/" }, params = { "page", "size", "filter" })
  public ResponseEntity<Map<String, Object>> all(
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "3") int size,
      @RequestParam String filter) {
    log.info("Accessing paginated and filtered Candidates");
    log.info("PAGE: " + page + " & SIZE: " + size + " & filtered by: " + filter);

    Map<String, Object> responseAll = this.candidateService.findByFilter(page, size, filter);
    return ResponseEntity.ok(responseAll);
  }

  @GetMapping(value = { "", "/" }, params = { "!page", "!size", "filter" })
  public ResponseEntity<Map<String, Object>> findByFilter(
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "10") int size,
      @RequestParam String filter) {
    log.info("Accessing paginated and filtered Candidates inside new-proposal");
    log.info("PAGE: " + page + " & SIZE: " + size + " & filtered by: " + filter);

    Map<String, Object> responseAll = this.candidateService.findByFilter(page, size, filter);
    return ResponseEntity.ok(responseAll);
  }

  @GetMapping("/{id}")
  public Candidate one(@PathVariable("id") Long id) {
    return candidateService.one(id);
  }

  @PostMapping({ "", "/" })
  public Candidate newCandidate(@RequestBody Candidate candidate) {
    log.info("Creating new candidate = " + candidate);
    return this.candidateService.save(candidate);
  }

  @PutMapping("/{id}")
  public Candidate replaceCandidate(@PathVariable("id") Long id, @RequestBody Candidate candidate) {
    log.info("Updated candidate with id = " + id + "\n Candidate = " + candidate);
    return this.candidateService.replace(id, candidate);
  }

  @ResponseBody
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void deleteCandidate(@PathVariable("id") Long id) {
    log.info("Deleting candidate with id = " + id);
    this.candidateService.delete(id);
  }
}
