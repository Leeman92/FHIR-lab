package dev.patricklehmann.fhirlab.patients.domain;

import dev.patricklehmann.fhirlab.patients.api.CreatePatientRequest;
import dev.patricklehmann.fhirlab.patients.application.exception.InvalidBirthdayException;
import dev.patricklehmann.fhirlab.shared.domain.Activatable;
import dev.patricklehmann.fhirlab.shared.domain.DomainText;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "patients")
@Getter
public class Patient implements Activatable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Embedded private PatientName patientName;

    @Column(nullable = false, name = "birth_date")
    private LocalDate birthDate;

    private boolean active;

    @Column(nullable = false, name = "idempotency_key")
    private String idempotencyKey;

    @Column(nullable = false, length = 64, name = "request_fingerprint")
    private String requestFingerprint;

    @CreationTimestamp private Instant createdAt;
    @UpdateTimestamp private Instant updatedAt;

    protected Patient() {}

    protected Patient(
            PatientName name,
            LocalDate birthDate,
            String idempotencyKey,
            String requestFingerprint) {
        this.active = true;

        this.patientName = name;
        this.birthDate = birthDate;
        this.idempotencyKey = idempotencyKey;
        this.requestFingerprint = requestFingerprint;
    }

    public static Patient register(
            PatientName patientName,
            LocalDate birthDate,
            LocalDate today,
            String idempotencyKey,
            String requestFingerprint) {
        Objects.requireNonNull(patientName);
        validateBirthDate(birthDate, today);
        idempotencyKey = DomainText.normalizeRequired(idempotencyKey, "idempotencyKey");
        requestFingerprint = DomainText.normalizeRequired(requestFingerprint, "fingerprint");

        return new Patient(patientName, birthDate, idempotencyKey, requestFingerprint);
    }

    public static Patient register(
            CreatePatientRequest request,
            LocalDate today,
            String idempotencyKey,
            String requestFingerprint) {
        return register(
                new PatientName(request.name().givenName(), request.name().familyName()),
                request.birthDate(),
                today,
                idempotencyKey,
                requestFingerprint);
    }

    private static void validateBirthDate(LocalDate birthDate, LocalDate today) {
        if (birthDate == null) {
            throw new InvalidBirthdayException(null);
        }
        Objects.requireNonNull(today, "Current date must not be null");

        if (birthDate.isAfter(today)) {
            throw new InvalidBirthdayException(birthDate);
        }
    }
}
