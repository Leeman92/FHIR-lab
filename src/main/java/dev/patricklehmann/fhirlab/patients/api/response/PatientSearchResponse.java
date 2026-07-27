package dev.patricklehmann.fhirlab.patients.api.response;

import dev.patricklehmann.fhirlab.patients.domain.Patient;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record PatientSearchResponse(@NotNull List<PatientResponse> foundPatients) {

    public PatientSearchResponse {
        foundPatients = foundPatients == null ? List.of() : List.copyOf(foundPatients);
    }

    public static PatientSearchResponse from(List<Patient> patients) {
        List<PatientResponse> searchResult = patients.stream().map(PatientResponse::from).toList();
        return new PatientSearchResponse(searchResult);
    }
}
