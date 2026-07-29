package dev.patricklehmann.fhirlab.patients.application;

import java.time.LocalDate;

/**
 * The criteria a patient search may narrow on (FR-003).
 *
 * <p>Lives in the application layer rather than beside the request DTOs because it is not a wire
 * contract: it is never deserialised, carries no validation constraints, and its field names are
 * not part of any published API. It is the parameter object of {@code PatientService#search}, so
 * every inbound adapter — the internal REST controller, the FHIR facade — depends on this layer
 * rather than on a sibling adapter's package.
 *
 * <p>Every component is optional. {@code null} means "do not filter on this", which is why {@code
 * active} is a boxed {@code Boolean}: absent, "only active" and "only inactive" are three distinct
 * requests. {@code given} and {@code family} match any part of the name, ignoring case.
 */
public record PatientSearchCriteria(
        String given, String family, LocalDate birthDate, Boolean active) {}
