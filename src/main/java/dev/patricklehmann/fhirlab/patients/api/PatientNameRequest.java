package dev.patricklehmann.fhirlab.patients.api;

import jakarta.validation.constraints.NotBlank;

public record PatientNameRequest(@NotBlank String givenName, @NotBlank String familyName) {}
