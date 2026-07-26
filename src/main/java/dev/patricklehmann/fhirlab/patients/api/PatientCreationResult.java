package dev.patricklehmann.fhirlab.patients.api;

import dev.patricklehmann.fhirlab.patients.domain.Patient;

public record PatientCreationResult(Patient patient, boolean replay) {}
