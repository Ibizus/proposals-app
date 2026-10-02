package org.propflow.back.controller;

import java.util.Date;
import java.util.Map;

import org.propflow.back.service.ProposalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = { "http://localhost:4200", "http://localhost:3000" })
@RequestMapping("/v1/api/dashboard")
public class DashboardController {

  @Autowired
  private ProposalService proposalService;

  @GetMapping("/summary")
  public ResponseEntity<?> getDashboardSummary() {
    Map<String, Object> summary = proposalService.getDashboardSummary();
    return ResponseEntity.ok(summary);
  }

  @GetMapping("/proposals/status")
  public ResponseEntity<?> getProposalsByStatus() {
    Map<String, Long> proposalsByStatus = proposalService.getProposalsByStatus();
    return ResponseEntity.ok(proposalsByStatus);
  }

  @GetMapping("/proposals/createdat")
  public ResponseEntity<?> getProposalsByCreatedAt() {
    Map<String, Integer> proposalsByCreatedAt = proposalService.getProposalsByCreatedAt();
    return ResponseEntity.ok(proposalsByCreatedAt);
  }
}
