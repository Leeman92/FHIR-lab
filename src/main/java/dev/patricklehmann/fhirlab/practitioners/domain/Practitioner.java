package dev.patricklehmann.fhirlab.practitioners.domain;

import dev.patricklehmann.fhirlab.shared.domain.Activatable;
import dev.patricklehmann.fhirlab.shared.domain.DomainText;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * A medical professional who carries out appointments (spec §6.2).
 *
 * <p>The display name is mandatory; the speciality is free text for now and is the field that maps
 * onto the FHIR {@code Practitioner} qualification. A deactivated practitioner stays visible and
 * keeps existing appointments, but accepts no new ones.
 */
@Entity
@Table(name = "practitioners")
@Getter
public class Practitioner implements Activatable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Setter(AccessLevel.NONE)
    private UUID id;

    private String displayName;
    // Will be updated to a class/enum in the future
    private String speciality;
    private boolean active;

    @CreationTimestamp private Instant createdAt;
    @UpdateTimestamp private Instant updatedAt;

    /** Required by JPA; not for application use. */
    protected Practitioner() {}

    /** Assumes normalised arguments — go through {@link #from} instead. */
    protected Practitioner(String displayName, String speciality) {
        this.displayName = displayName;
        this.speciality = speciality;
    }

    /**
     * Builds a practitioner with a normalised name and speciality.
     *
     * <p>Note that this does not yet set the active flag, so the practitioner is created inactive —
     * FR-005 requires a new practitioner to be active, and {@code Appointment.book} would refuse
     * one that is not.
     *
     * @throws IllegalArgumentException if either value is null or blank
     */
    public static Practitioner from(String displayName, String speciality) {
        displayName = DomainText.normalizeRequired(displayName, "Displayname cant be null");
        speciality = DomainText.normalizeRequired(speciality, "Speciality must be non null");

        return new Practitioner(displayName, speciality);
    }
}
