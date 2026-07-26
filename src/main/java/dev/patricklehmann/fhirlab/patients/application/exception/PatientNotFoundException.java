package dev.patricklehmann.fhirlab.patients.application.exception;

import dev.patricklehmann.fhirlab.shared.application.exception.CustomException;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

public class PatientNotFoundException extends CustomException {
    UUID patientId;

    public PatientNotFoundException(UUID patientId) {
        this.patientId = patientId;
        super("Patient with id '%s' was not found".formatted(patientId));
    }

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

    @Override
    public String getLogMessage() {
        return "Patient with id '%s' was not found".formatted(patientId);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
