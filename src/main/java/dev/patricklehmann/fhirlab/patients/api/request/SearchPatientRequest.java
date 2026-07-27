package dev.patricklehmann.fhirlab.patients.api.request;

import jakarta.validation.constraints.NotBlank;

public record SearchPatientRequest(@NotBlank String fullName, boolean active) {}
