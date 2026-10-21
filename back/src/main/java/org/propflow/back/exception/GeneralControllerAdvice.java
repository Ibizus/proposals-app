package org.propflow.back.exception;

import java.net.URI;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Turns every exception that escapes a controller into an RFC 7807 {@link ProblemDetail} response,
 * so error bodies share one shape across the API.
 */
@Slf4j
@ControllerAdvice
public class GeneralControllerAdvice extends ResponseEntityExceptionHandler {

  private static final URI ENTITY_NOT_FOUND = URI.create("urn:propflow:problem:entity-not-found");
  private static final URI COUPLING_ID_MISMATCH =
      URI.create("urn:propflow:problem:coupling-id-mismatch");
  private static final URI VALIDATION_FAILED = URI.create("urn:propflow:problem:validation-failed");
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

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException exception,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    List<FieldError> errors =
        exception.getBindingResult().getFieldErrors().stream()
            .map(
                fieldError -> new FieldError(fieldError.getField(), fieldError.getDefaultMessage()))
            .toList();

    ProblemDetail problem = exception.getBody();
    problem.setDetail("Request validation failed");
    problem.setTitle("Validation failed");
    problem.setType(VALIDATION_FAILED);
    problem.setProperty("errors", errors);
    return handleExceptionInternal(exception, problem, headers, status, request);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ProblemDetail> handleUnexpected(Exception exception) {
    log.error("Unhandled exception", exception);
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
