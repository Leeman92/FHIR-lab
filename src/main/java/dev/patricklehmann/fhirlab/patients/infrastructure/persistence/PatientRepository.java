package dev.patricklehmann.fhirlab.patients.infrastructure.persistence;

import dev.patricklehmann.fhirlab.patients.domain.Patient;
import dev.patricklehmann.fhirlab.shared.domain.EntityRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PatientRepository
        extends EntityRepository<Patient, UUID>, JpaSpecificationExecutor<Patient> {
    List<Patient> id(UUID id);

    Optional<Patient> findByIdempotencyKey(String idempotencyKey);
}
