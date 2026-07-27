package dev.patricklehmann.fhirlab.patients.application.exception;

import dev.patricklehmann.fhirlab.shared.application.exception.CustomException;
import java.net.URI;
import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

public class InvalidBirthdayException extends CustomException {
    LocalDate birthdate;

    public InvalidBirthdayException(LocalDate birthdate) {
        this.birthdate = birthdate;
        super("Birthday %s is invalid".formatted(birthdate));
    }

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

    @Override
    public String getLogMessage() {
        return "Birthday '%s' invalid".formatted(birthdate);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
