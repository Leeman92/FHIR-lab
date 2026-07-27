package dev.patricklehmann.fhirlab.patients.api;

import java.time.LocalDate;

public record PatientSearchCriteria(
    String given,
    String family,
    LocalDate birthDate,
    Boolean active
) {}
