package dev.patricklehmann.fhirlab.patients.application.exception;

import dev.patricklehmann.fhirlab.shared.application.exception.CustomException;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

/**
 * Raised when no patient carries the requested id (FR-002).
 *
 * <p>Maps to {@code 404} with the type {@code urn:problem:not-found}. The detail repeats the id
 * that was asked for, which FR-002 requires explicitly; the id is a synthetic UUID and carries
 * nothing personal.
 */
public class PatientNotFoundException extends CustomException {
    UUID patientId;

    /** {@code patientId} is the id that was looked up, and is echoed back to the caller. */
    public PatientNotFoundException(UUID patientId) {
        this.patientId = patientId;
        super("Patient with id '%s' was not found".formatted(patientId));
    }

    /** The client-facing problem detail. Contains no internal details (FR-032). */
    @Override
    public ProblemDetail getProblemDetail() {
        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(
                        getStatus(),
                        "The Patient with the id '%s' was not found".formatted(patientId));

        problem.setTitle("Patient not found");
        problem.setType(URI.create("urn:problem:not-found"));

        return problem;
    }

    /** The log-facing message, kept shorter and less revealing than the client detail. */
    @Override
    public String getLogMessage() {
        return "Patient with id '%s' was not found".formatted(patientId);
    }

    /** The HTTP status this failure maps to. */
    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
