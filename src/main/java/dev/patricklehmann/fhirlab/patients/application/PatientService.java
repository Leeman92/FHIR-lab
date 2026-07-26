package dev.patricklehmann.fhirlab.patients.application;

import dev.patricklehmann.fhirlab.patients.api.CreatePatientRequest;
import dev.patricklehmann.fhirlab.patients.api.PatientCreationResult;
import dev.patricklehmann.fhirlab.patients.application.exception.PatientNotFoundException;
import dev.patricklehmann.fhirlab.patients.domain.Patient;
import dev.patricklehmann.fhirlab.patients.domain.PatientRepository;
import dev.patricklehmann.fhirlab.shared.application.exception.IdempotencyConflictException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PatientService {
    private final PatientRepository patientRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public Patient getPatient(UUID patientId) {
        return patientRepository
                .findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId));
    }

    @Transactional
    public PatientCreationResult createPatient(
            String idempotencyKey, CreatePatientRequest request) {
        String uniqueFingerprint = createFingerprint(request);

        Optional<Patient> existingPatient = patientRepository.findByIdempotencyKey(idempotencyKey);

        if (existingPatient.isPresent()) {
            Patient persistedPatient = existingPatient.get();

            if (Objects.equals(persistedPatient.getRequestFingerprint(), uniqueFingerprint)) {
                return new PatientCreationResult(persistedPatient, true);
            }

            throw new IdempotencyConflictException(idempotencyKey);
        }

        Patient patient =
                Patient.register(request, LocalDate.now(clock), idempotencyKey, uniqueFingerprint);
        patientRepository.save(patient);

        return new PatientCreationResult(patient, false);
    }

    String createFingerprint(CreatePatientRequest request) {
        String normalized =
                request.name().givenName()
                        + ":"
                        + request.name().familyName()
                        + ":"
                        + request.birthDate().toString();

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(normalized.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 message digest is not available", exception);
        }
    }
}
