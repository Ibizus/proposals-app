package org.propflow.back.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.propflow.back.service.ProposalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
@RequestMapping("/v1/api/dashboard")
@Tag(name = "dashboard", description = "Dashboard aggregations")
public class DashboardController {

  @Autowired private ProposalService proposalService;

  @Operation(summary = "Get dashboard summary")
  @GetMapping("/summary")
  public ResponseEntity<?> getDashboardSummary() {
    Map<String, Object> summary = proposalService.getDashboardSummary();
    return ResponseEntity.ok(summary);
  }

  @Operation(summary = "Get proposal counts by status")
  @GetMapping("/proposals/status")
  public ResponseEntity<?> getProposalsByStatus() {
    Map<String, Long> proposalsByStatus = proposalService.getProposalsByStatus();
    return ResponseEntity.ok(proposalsByStatus);
  }

  @Operation(summary = "Get proposal counts by creation date")
  @GetMapping("/proposals/createdat")
  public ResponseEntity<?> getProposalsByCreatedAt() {
    Map<String, Integer> proposalsByCreatedAt = proposalService.getProposalsByCreatedAt();
    return ResponseEntity.ok(proposalsByCreatedAt);
  }
}
