package dev.patricklehmann.fhirlab.api.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.NonNull;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Assigns every request a correlation id (NFR-004).
 *
 * <p>The id is echoed in the response header, put on the MDC so that all logging for the request
 * carries it, and exposed as a request attribute for {@code GlobalExceptionHandler} to include in
 * error responses. A caller may supply its own id, but only if it is a canonical UUID — see {@link
 * #resolveRequestId}.
 */
@Component
public class RequestIdFilter extends OncePerRequestFilter {

    public static final String HEADER_NAME = "X-Request-ID";
    public static final String ATTRIBUTE_NAME = "requestId";
    public static final String MDC_KEY = "requestId";

    @Override
    /**
     * Establishes the request id for the duration of the request and clears the MDC afterwards, so
     * the value cannot leak into unrelated work on a pooled thread.
     */
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String requestId = resolveRequestId(request);

        request.setAttribute(ATTRIBUTE_NAME, requestId);
        response.setHeader(HEADER_NAME, requestId);
        MDC.put(MDC_KEY, requestId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }

    /**
     * Returns a trusted correlation id: the client header only if it parses as a canonical 36-char
     * UUID, otherwise a freshly generated one. Even a valid client value is re-serialized from the
     * parsed UUID so the echoed id is always canonical and never raw client input.
     */
    private String resolveRequestId(HttpServletRequest request) {
        String candidate = request.getHeader(HEADER_NAME);

        if (candidate == null || candidate.length() != 36) {
            return UUID.randomUUID().toString();
        }

        try {
            UUID uuid = UUID.fromString(candidate);

            // Return a newly generated canonical string rather than client input.
            return uuid.toString();
        } catch (IllegalArgumentException exception) {
            return UUID.randomUUID().toString();
        }
    }
}
