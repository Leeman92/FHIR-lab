package dev.patricklehmann.fhirlab.patients.infrastructure.persistence;

import dev.patricklehmann.fhirlab.patients.domain.Patient;
import dev.patricklehmann.fhirlab.shared.domain.EntityRepository;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Persistence for {@link Patient}.
 *
 * <p>{@link JpaSpecificationExecutor} supplies the {@code findAll(Specification)} used by the
 * search in FR-003; the criteria themselves are assembled from {@code PatientSpecifications}.
 */
public interface PatientRepository
        extends EntityRepository<Patient, UUID>, JpaSpecificationExecutor<Patient> {}
