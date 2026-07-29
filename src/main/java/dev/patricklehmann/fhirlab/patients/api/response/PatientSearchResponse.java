package dev.patricklehmann.fhirlab.patients.api.response;

import dev.patricklehmann.fhirlab.patients.domain.Patient;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * The result of a patient search (FR-003).
 *
 * <p>Always a list, never null: no match is an empty result rather than an error. Wrapping the list
 * in an object instead of returning a bare JSON array leaves room to add paging metadata later
 * without breaking clients.
 */
public record PatientSearchResponse(@NotNull List<PatientResponse> foundPatients) {

    /** Normalises null to an empty list and defensively copies, keeping the record immutable. */
    public PatientSearchResponse {
        foundPatients = foundPatients == null ? List.of() : List.copyOf(foundPatients);
    }

    /** Projects search hits onto the wire format, preserving the repository's order. */
    public static PatientSearchResponse from(List<Patient> patients) {
        List<PatientResponse> searchResult = patients.stream().map(PatientResponse::from).toList();
        return new PatientSearchResponse(searchResult);
    }
}
