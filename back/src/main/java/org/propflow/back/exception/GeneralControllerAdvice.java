package org.propflow.back.exception;

import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Turns every exception that escapes a controller into an RFC 7807 {@link ProblemDetail} response,
 * so error bodies share one shape across the API.
 */
@ControllerAdvice
public class GeneralControllerAdvice {

  private static final URI ENTITY_NOT_FOUND = URI.create("urn:propflow:problem:entity-not-found");
  private static final URI COUPLING_ID_MISMATCH =
      URI.create("urn:propflow:problem:coupling-id-mismatch");
  private static final URI VALIDATION_FAILED = URI.create("urn:propflow:problem:validation-failed");
  private static final URI MALFORMED_REQUEST = URI.create("urn:propflow:problem:malformed-request");
  private static final URI INTERNAL_ERROR = URI.create("urn:propflow:problem:internal-error");

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleEntityNotFound(EntityNotFoundException exception) {
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    problem.setTitle("Entity not found");
    problem.setType(ENTITY_NOT_FOUND);
    return problemResponse(HttpStatus.NOT_FOUND, problem);
  }

  @ExceptionHandler(NotCouplingIdException.class)
  public ResponseEntity<ProblemDetail> handleNotCouplingId(NotCouplingIdException exception) {
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
    problem.setTitle("Identifier mismatch");
    problem.setType(COUPLING_ID_MISMATCH);
    return problemResponse(HttpStatus.CONFLICT, problem);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException exception) {
    List<FieldError> errors =
        exception.getBindingResult().getFieldErrors().stream()
            .map(
                fieldError -> new FieldError(fieldError.getField(), fieldError.getDefaultMessage()))
            .toList();

    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request validation failed");
    problem.setTitle("Validation failed");
    problem.setType(VALIDATION_FAILED);
    problem.setProperty("errors", errors);
    return problemResponse(HttpStatus.BAD_REQUEST, problem);
  }

  @ExceptionHandler({
    HttpMessageNotReadableException.class,
    MethodArgumentTypeMismatchException.class
  })
  public ResponseEntity<ProblemDetail> handleMalformedRequest(Exception exception) {
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, "The request body or parameters are malformed");
    problem.setTitle("Malformed request");
    problem.setType(MALFORMED_REQUEST);
    return problemResponse(HttpStatus.BAD_REQUEST, problem);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ProblemDetail> handleUnexpected(Exception exception) {
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    problem.setTitle("Internal Server Error");
    problem.setType(INTERNAL_ERROR);
    return problemResponse(HttpStatus.INTERNAL_SERVER_ERROR, problem);
  }

  private static ResponseEntity<ProblemDetail> problemResponse(
      HttpStatus status, ProblemDetail problem) {
    return ResponseEntity.status(status)
        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
        .body(problem);
  }

  /** A single field-level validation error attached to a ProblemDetail response. */
  public record FieldError(String field, String message) {}
}
