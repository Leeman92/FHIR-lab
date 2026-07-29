package dev.patricklehmann.fhirlab.patients.api.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Inbound payload for registering a patient (FR-001).
 *
 * <p>The constraints here cover the shape of the request; the rule that a birth date may not lie in
 * the future is a domain rule and is enforced by {@code Patient.register}. That split is also why a
 * request that is wrong in both respects currently reports only the field errors — see FR-030.
 */
public record CreatePatientRequest(
        @NotNull @Valid PatientNameRequest name, @NotNull LocalDate birthDate) {}
