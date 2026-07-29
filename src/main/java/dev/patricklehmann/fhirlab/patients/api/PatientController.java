package dev.patricklehmann.fhirlab.patients.api;

import dev.patricklehmann.fhirlab.patients.api.request.CreatePatientRequest;
import dev.patricklehmann.fhirlab.patients.api.response.PatientResponse;
import dev.patricklehmann.fhirlab.patients.api.response.PatientSearchResponse;
import dev.patricklehmann.fhirlab.patients.application.PatientSearchCriteria;
import dev.patricklehmann.fhirlab.patients.application.PatientService;
import dev.patricklehmann.fhirlab.patients.domain.Patient;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal HTTP API for patients (FR-001 to FR-004).
 *
 * <p>This is the project's own representation, deliberately separate from the FHIR facade: the FHIR
 * {@code Patient} resource is produced from the same domain entities but has its own controller and
 * its own DTOs, so changes to the exchange format cannot ripple into this one (FR-028).
 *
 * <p>Errors are not handled here. Business failures are raised as {@code CustomException}s and
 * rendered as RFC 9457 problem details by {@code GlobalExceptionHandler}, which also attaches the
 * request id and timestamp every response needs (FR-029).
 */
@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
public class PatientController {
    private final PatientService patientService;

    /**
     * Registers a patient and answers {@code 201 Created} with a {@code Location} header pointing
     * at the new resource (FR-001).
     *
     * <p>Field-level validation happens through {@code @Valid} before the domain is reached, so a
     * malformed request yields a collected list of field errors rather than the first failure.
     * Creating the same patient twice creates two records — see {@code
     * PatientService#createPatient}.
     */
    @PostMapping("")
    public ResponseEntity<PatientResponse> createPatient(
            @Valid @RequestBody CreatePatientRequest request) {
        Patient patient = patientService.createPatient(request);
        PatientResponse response = PatientResponse.from(patient);

        return ResponseEntity.created(URI.create("/patients/" + patient.getId())).body(response);
    }

    /**
     * Returns one patient's administrative data (FR-002). An unknown id produces a not-found
     * problem detail that names the id that was asked for.
     */
    @GetMapping("/{id}")
    public PatientResponse getPatient(@PathVariable @Valid UUID id) {
        Patient patient = patientService.getPatient(id);
        return PatientResponse.from(patient);
    }

    /**
     * Deactivates a patient (FR-004). The call is idempotent: repeating it returns the same
     * unchanged representation instead of failing.
     *
     * <p>Modelled as a named action rather than {@code DELETE} or a {@code PATCH} of the active
     * flag, because deactivation is a business operation with its own rules — the record survives,
     * stays retrievable, keeps its appointments, and only stops accepting new ones.
     */
    @PostMapping("/{id}/deactivate")
    public PatientResponse deactivatePatient(@PathVariable UUID id) {
        Patient patient = patientService.deactivatePatient(id);
        return PatientResponse.from(patient);
    }

    /**
     * Searches patients (FR-003). Every parameter is optional and they are combined with AND; name
     * matching is case-insensitive and matches any part of the name. No match is an empty result,
     * not an error.
     *
     * <p>{@code birthDate} is not required by FR-003 but is supported because the FHIR {@code
     * Patient} search defines an equivalent {@code birthdate} parameter.
     */
    @GetMapping
    public PatientSearchResponse searchPatients(
            @RequestParam(required = false) String given,
            @RequestParam(required = false) String family,
            @RequestParam(required = false) LocalDate birthDate,
            @RequestParam(required = false) Boolean active) {
        PatientSearchCriteria criteria =
                new PatientSearchCriteria(given, family, birthDate, active);
        return PatientSearchResponse.from(patientService.search(criteria));
    }
}
