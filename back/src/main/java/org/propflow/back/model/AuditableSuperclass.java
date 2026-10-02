package org.propflow.back.model;

import java.time.LocalDateTime;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
@SuppressWarnings("unused")
public class AuditableSuperclass {

  // Attributes to save auditable data:
  private LocalDateTime createdAt = LocalDateTime.now();
  private String createdBy;
  private LocalDateTime modifiedAt = this.createdAt;
  private String modifiedBy;

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
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
}
