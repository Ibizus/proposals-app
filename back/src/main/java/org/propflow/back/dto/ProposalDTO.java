package org.propflow.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import org.propflow.back.model.Candidate;
import org.propflow.back.model.Salary;
import org.propflow.back.model.Status;

/**
 * This DTO was created to solve a problem where fetching a proposal would have an Office object
 * with a set of Proposals and it can result in an infinite loop. So ProposalDTO has an OfficeDTO
 * property instead of an Office, and the OfficeDTO has a set of the id's of the proposals it has,
 * so this should solve the infinite loop problem
 */
@Schema(
    description =
        "Proposal payload returned by the API; the office is reduced to its identifiers to avoid reference cycles.")
public class ProposalDTO {
  private long id;
  private String description;
  private String role;
  private String contractLevel;
  private Boolean indefinite;
  private LocalDate startDate;
  private LocalDate dueDate;
  private Integer status;
  private String knowMore;
  private OfficeDTO office;
  private Salary salary;
  private Candidate candidate;
  private Set<Status> statuses;
  private String njoyn;
  private LocalDateTime createdAt = LocalDateTime.now();
  private String createdBy;
  private LocalDateTime modifiedAt = this.createdAt;
  private String modifiedBy;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public String getContractLevel() {
    return contractLevel;
  }

  public void setContractLevel(String contractLevel) {
    this.contractLevel = contractLevel;
  }

  public Boolean getIndefinite() {
    return indefinite;
  }

  public void setIndefinite(Boolean indefinite) {
    this.indefinite = indefinite;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public void setStartDate(LocalDate startDate) {
    this.startDate = startDate;
  }

  public LocalDate getDueDate() {
    return dueDate;
  }

  public void setDueDate(LocalDate dueDate) {
    this.dueDate = dueDate;
  }

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public String getKnowMore() {
    return knowMore;
  }

  public void setKnowMore(String knowMore) {
    this.knowMore = knowMore;
  }

  public OfficeDTO getOffice() {
    return office;
  }

  public void setOffice(OfficeDTO office) {
    this.office = office;
  }

  public Salary getSalary() {
    return salary;
  }

  public void setSalary(Salary salary) {
    this.salary = salary;
  }

  public Candidate getCandidate() {
    return candidate;
  }

  public void setCandidate(Candidate candidate) {
    this.candidate = candidate;
  }

  public String getNjoyn() {
    return njoyn;
  }

  public void setNjoyn(String njoyn) {
    this.njoyn = njoyn;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getModifiedAt() {
    return modifiedAt;
  }

  public void setModifiedAt(LocalDateTime modifiedAt) {
    this.modifiedAt = modifiedAt;
  }

  public String getModifiedBy() {
    return modifiedBy;
  }

  public void setModifiedBy(String modifiedBy) {
    this.modifiedBy = modifiedBy;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public Set<Status> getStatuses() {
    return statuses;
  }

  public void setStatuses(Set<Status> statuses) {
    this.statuses = statuses;
  }
}
