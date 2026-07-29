package dev.patricklehmann.fhirlab.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Makes the current time an injected dependency.
 *
 * <p>Rules that turn on "now" — a birth date must not lie in the future, an appointment must begin
 * in the future, a past appointment may be completed — are only testable if the clock can be
 * replaced, so nothing in this project calls {@code Instant.now()} directly.
 */
@Configuration(proxyBeanMethods = false)
public class TimeConfiguration {
    @Bean
    /** UTC, so stored instants never depend on the host's zone (NFR-005). */
    Clock clock() {
        return Clock.systemUTC();
    }
}
