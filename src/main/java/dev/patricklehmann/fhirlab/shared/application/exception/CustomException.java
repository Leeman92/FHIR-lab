package dev.patricklehmann.fhirlab.shared.application.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

@Getter
/**
 * Base class for failures that are part of the domain rather than bugs.
 *
 * <p>Subclasses decide three things independently: how the failure is presented to the caller
 * ({@link #getProblemDetail()}), how it is recorded in the log ({@link #getLogMessage()}), and
 * which HTTP status it maps to ({@link #getStatus()}). Splitting the client message from the log
 * message lets the log stay terse and free of personal data (NFR-010) while the response stays
 * helpful.
 *
 * <p>Anything that escapes as a plain {@code RuntimeException} is treated as unexpected and becomes
 * a {@code 500}, so a rule that should reach the caller as a 4xx belongs in a subclass here.
 */
public abstract class CustomException extends RuntimeException {

    /**
     * {@code message} is the developer-facing text carried by the exception itself; what the caller
     * and the log see is decided by the abstract methods below.
     */
    public CustomException(String message) {
        super(message);
    }

    /** The client-facing representation, free of internal details (FR-032). */
    public abstract ProblemDetail getProblemDetail();

    /** A terse description for the log, with as little personal data as possible. */
    public abstract String getLogMessage();

    /** The HTTP status this failure maps to. */
    public abstract HttpStatus getStatus();
}
