package dev.patricklehmann.fhirlab.patients.api.request;

import jakarta.validation.constraints.NotBlank;

/**
 * A patient's name as it arrives over the wire.
 *
 * <p>{@code @NotBlank} rejects names that are empty or whitespace-only, using the same
 * Unicode-aware definition of "blank" that {@code DomainText} applies one layer down. Surrounding
 * whitespace is tolerated here and normalised away when the name enters the domain.
 */
public record PatientNameRequest(@NotBlank String givenName, @NotBlank String familyName) {}
