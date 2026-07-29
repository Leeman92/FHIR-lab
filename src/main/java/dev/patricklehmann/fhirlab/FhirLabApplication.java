package dev.patricklehmann.fhirlab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for PraxisSlot, an appointment manager with a read-only FHIR facade.
 *
 * <p>The code is arranged by business area — patients, practitioners, rooms, appointments — and
 * each area is layered as {@code api} / {@code application} / {@code domain} / {@code
 * infrastructure}, so that the FHIR mapping can be changed without disturbing the appointment logic
 * (NFR-011).
 */
@SpringBootApplication
public class FhirLabApplication {

    /** Boots the application; startup is logged by Spring Boot itself (NFR-006). */
    public static void main(String[] args) {
        SpringApplication.run(FhirLabApplication.class, args);
    }
}
