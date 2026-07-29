package dev.patricklehmann.fhirlab.api.error;

import dev.patricklehmann.fhirlab.api.filters.RequestIdFilter;
import dev.patricklehmann.fhirlab.shared.application.exception.CustomException;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Renders every failure outside the FHIR facade as an RFC 9457 problem detail (FR-029).
 *
 * <p>Each response carries a stable type URN, a title, a human-readable description, the status, a
 * timestamp and the request id, with field-level errors added where they exist. Internal detail —
 * stack traces, class names, SQL — never reaches the client (FR-032): anything that is not a {@code
 * CustomException} is reported as a generic internal error and only the log keeps the cause.
 */
@NullMarked
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    Clock clock;

    /** The clock supplies the timestamp on every problem detail, so tests can pin it. */
    public GlobalExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    @ExceptionHandler(CustomException.class)
    /**
     * Handles the project's own business failures, which decide their own status and presentation.
     *
     * <p>Note that this logs at {@code ERROR} with the client-facing detail, so a routine 404 is
     * recorded as an error and personal data can reach the log; {@code
     * CustomException.getLogMessage()} exists to avoid exactly that and is currently unused
     * (NFR-006, NFR-010).
     */
    public ResponseEntity<Object> handleCustomException(
            CustomException exception, WebRequest request) {
        ProblemDetail problem = exception.getProblemDetail();

        log.error(problem.getDetail(), exception);

        return createResponseEntity(problem, HttpHeaders.EMPTY, exception.getStatus(), request);
    }

    @ExceptionHandler(Exception.class)
    /**
     * Last-resort handler for anything unforeseen (FR-031's internal error).
     *
     * <p>The cause is logged in full and the caller receives only a generic message plus the
     * request id to quote. Because this catches {@code Exception}, a domain rule that throws a
     * plain runtime exception silently becomes a 500 instead of a 4xx — such rules belong in a
     * {@code CustomException} subclass.
     */
    public ResponseEntity<Object> handleUnexpectedException(
            Exception exception, WebRequest request) {
        log.error("Unhandled exception while processing request", exception);

        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        """
                Something went wrong. If the error persists, please contact \
                support and provide the request ID.
                """);

        problem.setTitle("Internal Server Error");
        problem.setType(URI.create("urn:problem:internal-server-error"));

        return createResponseEntity(
                problem, HttpHeaders.EMPTY, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @Override
    /**
     * Reports failed request validation as one response listing every offending field, so a caller
     * does not have to discover mistakes one round-trip at a time (FR-030).
     *
     * <p>Only the first message per field is kept. Rules enforced in the domain rather than by a
     * field constraint are not part of this collection.
     */
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }

        log.info("Request validation failed: fields={}", fieldErrors.keySet());

        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST, "The request contains invalid fields.");

        problem.setTitle("Validation failed");
        problem.setType(URI.create("urn:problem:validation-error"));
        problem.setProperty("errors", fieldErrors);

        return createResponseEntity(problem, headers, HttpStatus.BAD_REQUEST, request);
    }

    @Override
    /**
     * Completes every problem detail on its way out, including those produced by Spring's own
     * handlers.
     *
     * <p>Adds the request id and timestamp, fills in the instance from the request path, and
     * supplies a type URN where the originating handler left none, so that the shape of an error
     * response is the same no matter which layer produced it (FR-029).
     */
    protected ResponseEntity<Object> createResponseEntity(
            @Nullable Object body,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request) {
        if (body instanceof ProblemDetail problem
                && request instanceof ServletWebRequest servletRequest) {

            HttpServletRequest httpRequest = servletRequest.getRequest();
            Object requestId = httpRequest.getAttribute(RequestIdFilter.ATTRIBUTE_NAME);
            if (requestId != null) {
                problem.setProperty("requestId", requestId);
            }

            Object type = problem.getType();
            if (type == null) {
                String uri = "urn:problem:internal-server-error";
                if (statusCode.isSameCodeAs(HttpStatus.NOT_FOUND)) {
                    uri = "urn:problem:not-found";
                } else if (statusCode.is4xxClientError()) {
                    uri = "urn:problem:client-error";
                }

                problem.setType(URI.create(uri));
            }

            problem.setProperty("timestamp", Instant.now(clock));
            if (problem.getInstance() == null) {
                problem.setInstance(URI.create(httpRequest.getRequestURI()));
            }
        }

        return super.createResponseEntity(body, headers, statusCode, request);
    }
}
