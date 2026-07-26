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

@NullMarked
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    Clock clock;

    public GlobalExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Object> handleCustomException(
            CustomException exception, WebRequest request) {
        log.error(exception.getLogMessage(), exception);

        ProblemDetail problem = exception.getProblemDetail();
        return createResponseEntity(problem, HttpHeaders.EMPTY, exception.getStatus(), request);
    }

    @ExceptionHandler(Exception.class)
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
