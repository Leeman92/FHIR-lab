package dev.patricklehmann.fhirlab.patients.domain;

import dev.patricklehmann.fhirlab.shared.domain.Activatable;
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

    private LocalDate birthDate;
    private boolean active;

    @CreationTimestamp private Instant createdAt;
    @UpdateTimestamp private Instant updatedAt;

    protected Patient() {}

    protected Patient(PatientName name, LocalDate birthDate) {
        this.patientName = name;
        this.birthDate = birthDate;
    }

    public static Patient register(PatientName patientName, LocalDate birthDate, LocalDate today) {
        Objects.requireNonNull(patientName);
        validateBirthDate(birthDate, today);

        return new Patient(patientName, birthDate);
    }

    private static void validateBirthDate(LocalDate birthDate, LocalDate today) {
        Objects.requireNonNull(birthDate, "Birth date must not be null");
        Objects.requireNonNull(today, "Current date must not be null");

        if (birthDate.isAfter(today)) {
            throw new IllegalArgumentException("Birth date must not be in the future");
        }
    }
}
