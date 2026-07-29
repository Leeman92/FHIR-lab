package dev.patricklehmann.fhirlab.patients.api.response;

import dev.patricklehmann.fhirlab.patients.domain.Patient;
import dev.patricklehmann.fhirlab.patients.domain.PatientName;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * A patient as the internal API represents it (FR-002).
 *
 * <p>Carries the full administrative record, including the audit timestamps required by NFR-005.
 * Deliberately distinct from the FHIR {@code Patient} resource so the two formats can evolve
 * independently (FR-028).
 */
public record PatientResponse(
        UUID id,
        PatientName patientName,
        LocalDate birthDate,
        boolean active,
        Instant createdAt,
        Instant updatedAt) {
    /** Projects a domain entity onto the wire format. */
    public static PatientResponse from(Patient p) {
        return new PatientResponse(
                p.getId(),
                p.getPatientName(),
                p.getBirthDate(),
                p.isActive(),
                p.getCreatedAt(),
                p.getUpdatedAt());
    }
}
