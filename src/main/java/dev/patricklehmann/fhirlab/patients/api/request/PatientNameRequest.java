package dev.patricklehmann.fhirlab.patients.api.request;

import jakarta.validation.constraints.NotBlank;

public record PatientNameRequest(@NotBlank String givenName, @NotBlank String familyName) {}
