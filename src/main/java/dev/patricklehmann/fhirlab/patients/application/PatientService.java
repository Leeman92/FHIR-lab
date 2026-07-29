package dev.patricklehmann.fhirlab.patients.application;

import dev.patricklehmann.fhirlab.patients.api.request.CreatePatientRequest;
import dev.patricklehmann.fhirlab.patients.application.exception.InvalidBirthdayException;
import dev.patricklehmann.fhirlab.patients.application.exception.PatientNotFoundException;
import dev.patricklehmann.fhirlab.patients.domain.Patient;
import dev.patricklehmann.fhirlab.patients.domain.PatientName;
import dev.patricklehmann.fhirlab.patients.infrastructure.persistence.PatientRepository;
import dev.patricklehmann.fhirlab.patients.infrastructure.persistence.PatientSpecifications;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use cases for the patient aggregate (FR-001 to FR-004).
 *
 * <p>Business rules live on {@link Patient} itself; this class owns transaction boundaries,
 * repository access and the translation of search criteria into specifications. It hands back
 * domain entities and leaves the choice of representation to the caller, so the same use cases can
 * serve both the internal API and the FHIR facade (FR-028).
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PatientService {
    private final PatientRepository patientRepository;
    private final Clock clock;

    /**
     * Returns the patient with the given id (FR-002). Deactivated patients are returned like any
     * other; being inactive only blocks new appointments.
     *
     * @throws PatientNotFoundException if no patient carries that id
     */
    @Transactional(readOnly = true)
    public Patient getPatient(UUID patientId) {
        return patientRepository
                .findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId));
    }

    /**
     * Deactivates the patient with the given id and returns the resulting state (FR-004).
     *
     * <p>Deactivating an already inactive patient is a no-op that returns the unchanged record, so
     * the call is safe to repeat (NFR-003). The status change is logged once, by id only, to keep
     * personal data out of the log (NFR-006, NFR-010).
     *
     * @throws PatientNotFoundException if no patient carries that id
     */
    @Transactional
    public Patient deactivatePatient(UUID patientId) {
        Patient patient = getPatient(patientId);

        boolean wasActive = patient.isActive();
        patient.deactivate();

        if (wasActive) {
            log.info("Patient deactivated: id={}", patientId);
        }

        return patient;
    }

    /**
     * Registers a new patient (FR-001).
     *
     * <p>Deliberately not idempotent: two identical requests create two patients. There is no
     * natural key that could identify a duplicate — two people may share a name and a birth date —
     * and NFR-003 explicitly exempts repeated creation from the idempotency requirement.
     *
     * @throws InvalidBirthdayException if the birth date lies in the future
     */
    @Transactional
    public Patient createPatient(CreatePatientRequest request) {
        Patient patient =
                Patient.register(
                        PatientName.from(request.name().givenName(), request.name().familyName()),
                        request.birthDate(),
                        LocalDate.now(clock));

        return patientRepository.save(patient);
    }

    /**
     * Searches patients by any combination of the supported criteria (FR-003).
     *
     * <p>Criteria are combined with AND, and blank or absent values are dropped rather than matched
     * against, so a request without criteria lists every patient. No match is an empty list, not an
     * error.
     *
     * <p>Deactivated patients are included unless {@code active} narrows them out — the answer to
     * open question 4 in spec §16. The result is currently unbounded; open question 5 (how to limit
     * large result sets) is still undecided.
     */
    @Transactional(readOnly = true)
    public List<Patient> search(PatientSearchCriteria criteria) {
        List<Specification<Patient>> specifications = new ArrayList<>();

        if (hasText(criteria.given())) {
            specifications.add(PatientSpecifications.givenNameContaining(criteria.given()));
        }

        if (hasText(criteria.family())) {
            specifications.add(PatientSpecifications.familyNameContaining(criteria.family()));
        }

        if (criteria.birthDate() != null) {
            specifications.add(PatientSpecifications.hasBirthDate(criteria.birthDate()));
        }

        if (criteria.active() != null) {
            specifications.add(PatientSpecifications.hasActiveStatus(criteria.active()));
        }

        Specification<Patient> specification = Specification.allOf(specifications);

        return patientRepository.findAll(specification);
    }

    /** True when the value carries something worth filtering on. */
    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
