package dev.patricklehmann.fhirlab.patients.api;

import dev.patricklehmann.fhirlab.patients.application.PatientService;
import dev.patricklehmann.fhirlab.patients.domain.Patient;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
public class PatientController {
    private final PatientService patientService;

    @PostMapping({"", "/"})
    public ResponseEntity<PatientResponse> createPatient(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreatePatientRequest request) {
        PatientCreationResult result = patientService.createPatient(idempotencyKey, request);

        HttpStatus status = result.replay() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(PatientResponse.from(result.patient()));
    }

    @GetMapping({"{id}", "/{id}"})
    public PatientResponse getPatient(@PathVariable @Valid UUID id) {
        Patient patient = patientService.getPatient(id);
        return PatientResponse.from(patient);
    }
}
