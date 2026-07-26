package dev.patricklehmann.fhirlab.patients.api;

import dev.patricklehmann.fhirlab.patients.domain.Patient;
import dev.patricklehmann.fhirlab.patients.domain.PatientName;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PatientResponse(
        UUID id,
        PatientName patientName,
        LocalDate birthDate,
        boolean active,
        Instant createdAt,
        Instant updatedAt) {
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
