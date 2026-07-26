package dev.patricklehmann.fhirlab.patients.domain;

import dev.patricklehmann.fhirlab.shared.domain.EntityRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PatientRepository extends EntityRepository<Patient, UUID> {
    List<Patient> id(UUID id);

    Optional<Patient> findByIdempotencyKey(String idempotencyKey);
}
