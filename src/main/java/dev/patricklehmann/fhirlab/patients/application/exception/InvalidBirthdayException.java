package dev.patricklehmann.fhirlab.patients.application.exception;

import dev.patricklehmann.fhirlab.shared.application.exception.CustomException;
import java.net.URI;
import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

/**
 * Raised when a patient's birth date is missing or lies in the future (spec §6.1).
 *
 * <p>Maps to {@code 400} with the type {@code urn:problem:invalid-birthdate}, which distinguishes
 * it from other invalid input as FR-031 requires.
 */
public class InvalidBirthdayException extends CustomException {
    LocalDate birthdate;

    /** {@code birthdate} may be null, which is itself one of the rejected cases. */
    public InvalidBirthdayException(LocalDate birthdate) {
        this.birthdate = birthdate;
        super("Birthday %s is invalid".formatted(birthdate));
    }

    /** The client-facing problem detail. Contains no internal details (FR-032). */
    @Override
    public ProblemDetail getProblemDetail() {
        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(
                        getStatus(),
                        "The passed Birthday '%s' is invalid. It may not be in the future"
                                .formatted(birthdate));

        problem.setTitle("Invalid birthday");
        problem.setType(URI.create("urn:problem:invalid-birthdate"));

        return problem;
    }

    /** The log-facing message, kept shorter and less revealing than the client detail. */
    @Override
    public String getLogMessage() {
        return "Birthday '%s' invalid".formatted(birthdate);
    }

    /** The HTTP status this failure maps to. */
    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
