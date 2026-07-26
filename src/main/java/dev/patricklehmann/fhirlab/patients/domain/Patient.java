package dev.patricklehmann.fhirlab.patients.domain;

import dev.patricklehmann.fhirlab.shared.domain.Activatable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name= "patients")
@Getter
public class Patient implements Activatable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Setter(AccessLevel.NONE)
    private UUID id;

    @Embedded
    private PatientName patientName;

    private LocalDate birthDate;
    private boolean active;

    @CreationTimestamp
    private Instant createdAt;
    @UpdateTimestamp
    private Instant updatedAt;

    protected Patient() {}

    public Patient(
        PatientName patientName,
        LocalDate birthDate,
        LocalDate today
    ) {
        this.patientName = Objects.requireNonNull(patientName);
        this.birthDate = validateBirthDate(birthDate, today);
    }

    private static LocalDate validateBirthDate(LocalDate birthDate, LocalDate today) {
        Objects.requireNonNull(birthDate, "Birth date must not be null");
        Objects.requireNonNull(today, "Current date must not be null");

        if (birthDate.isAfter(today)) {
            throw new IllegalArgumentException("Birth date must not be in the future");
        }

        return birthDate;
    }
}
