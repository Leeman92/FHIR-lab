package dev.patricklehmann.fhirlab.patients.api.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Payload for a search across the whole name rather than a single field.
 *
 * <p>Currently unused: the search endpoint takes discrete {@code given} and {@code family}
 * parameters. FR-003 also calls for matching "part of a name", which this record would serve once a
 * combined parameter is added; until then it is dead weight and can be deleted.
 */
public record SearchPatientRequest(@NotBlank String fullName, boolean active) {}
