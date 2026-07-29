package dev.patricklehmann.fhirlab.patients.domain;

import dev.patricklehmann.fhirlab.patients.application.exception.InvalidBirthdayException;
import dev.patricklehmann.fhirlab.shared.domain.Activatable;
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

/**
 * A person appointments can be booked for (spec §6.1).
 *
 * <p>Instances are only reachable through {@link #register}, which enforces the invariants the
 * requirements place on a patient: the name must be present and normalised, and the birth date must
 * not lie in the future. A patient is active from the moment it is registered.
 *
 * <p>Deactivation is a state change rather than a deletion — see {@link #deactivate()}.
 */
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

    @Column(nullable = false)
    private boolean active;

    @CreationTimestamp private Instant createdAt;
    @UpdateTimestamp private Instant updatedAt;

    /** Required by JPA; not for application use. */
    protected Patient() {}

    /** Assumes validated arguments — go through {@link #register} instead. */
    protected Patient(PatientName name, LocalDate birthDate) {
        this.active = true;

        this.patientName = name;
        this.birthDate = birthDate;
    }

    /**
     * Registers a new, active patient (FR-001).
     *
     * <p>{@code today} is passed in rather than read from the system clock so that the rule "the
     * birth date must not lie in the future" stays deterministic and testable; a birth date equal
     * to {@code today} is accepted.
     *
     * @throws InvalidBirthdayException if the birth date is missing or in the future
     */
    public static Patient register(PatientName patientName, LocalDate birthDate, LocalDate today) {
        Objects.requireNonNull(patientName);
        validateBirthDate(birthDate, today);

        return new Patient(patientName, birthDate);
    }

    /**
     * Deactivates this patient. A deactivated patient stays retrievable and keeps its existing
     * appointments; only new appointments are refused (see {@code Appointment.book}). Repeating the
     * call is a no-op, so the modification timestamp is left untouched.
     */
    public void deactivate() {
        if (!active) {
            return;
        }

        this.active = false;
    }

    /** Rejects a missing birth date and one that lies after {@code today} (spec §6.1). */
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
