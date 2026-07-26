package dev.patricklehmann.fhirlab.shared.application.exception;

import java.net.URI;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

@Getter
public class IdempotencyConflictException extends CustomException {
    private final String idempotencyKey;

    public IdempotencyConflictException(String idempotencyKey) {
        super(
                "Idempotency key '%s' was already used for a different request"
                        .formatted(idempotencyKey));
        this.idempotencyKey = idempotencyKey;
    }

    @Override
    public ProblemDetail getProblemDetail() {
        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(
                        getStatus(),
                        """
                This seems to be a duplicate request. Please retry again!
                """);

        problem.setTitle("Duplicate Request");
        problem.setType(URI.create("urn:problem:duplicate-request"));

        return problem;
    }

    @Override
    public String getLogMessage() {
        return "Idempotency key '%s' was already used for a different request";
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
