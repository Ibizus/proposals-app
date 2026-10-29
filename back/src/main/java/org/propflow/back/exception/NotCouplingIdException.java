package org.propflow.back.exception;

public class NotCouplingIdException extends RuntimeException {
  @SuppressWarnings("rawtypes")
  public NotCouplingIdException(Long id1, Long id2, Class entity) {
    super(
        "Not coupling id1 = "
            + id1
            + " with id2 = "
            + id2
            + " for entity "
            + entity.getSimpleName());
  }
}
