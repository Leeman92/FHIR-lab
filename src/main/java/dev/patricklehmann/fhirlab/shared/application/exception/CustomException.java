package dev.patricklehmann.fhirlab.shared.application.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

@Getter
public abstract class CustomException extends RuntimeException {

    public CustomException(String message) {
        super(message);
    }

    public abstract ProblemDetail getProblemDetail();

    public abstract String getLogMessage();

    public abstract HttpStatus getStatus();
}
