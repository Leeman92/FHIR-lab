package dev.patricklehmann.fhirlab.appointments.domain;

import dev.patricklehmann.fhirlab.patients.domain.Patient;
import dev.patricklehmann.fhirlab.practitioners.domain.Practitioner;
import dev.patricklehmann.fhirlab.rooms.domain.Room;
import dev.patricklehmann.fhirlab.shared.domain.Activatable;
import dev.patricklehmann.fhirlab.shared.domain.DomainText;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * A reserved period between one patient, one practitioner and one room (spec §6.4).
 *
 * <p>Exactly one of each participant is required, and all three must be active at booking time. An
 * appointment is created as {@code BOOKED} and moves on from there; cancelling keeps the record and
 * releases the period, while completing it or marking it as a no-show freezes it.
 *
 * <p>Overlap is not enforced here. Three {@code EXCLUDE} constraints in the schema make a
 * double-booked patient, practitioner or room unrepresentable, which is what keeps two concurrent
 * bookings from both succeeding (NFR-001) without the application taking locks. The Java-side rules
 * exist to give callers a clear message first, not as the last line of defence.
 */
@Entity
@Table(name = "appointments")
@Getter
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Setter(AccessLevel.NONE)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "practitioner_id", nullable = false)
    private Practitioner practitioner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Embedded private AppointmentSlot appointmentSlot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppointmentStatus status;

    private String reason;

    @CreationTimestamp private Instant createdAt;
    @UpdateTimestamp private Instant updatedAt;

    /** Assumes validated arguments — go through {@link #book} instead. */
    protected Appointment(
            Patient patient,
            Practitioner practitioner,
            Room examinationRoom,
            AppointmentSlot slot,
            String reason) {
        this.patient = patient;
        this.practitioner = practitioner;
        this.room = examinationRoom;
        this.reason = reason;
        this.appointmentSlot = slot;

        this.status = AppointmentStatus.BOOKED;
    }

    /** Required by JPA; not for application use. */
    public Appointment() {}

    /**
     * Books a new appointment with status {@code BOOKED} (FR-011).
     *
     * <p>Verifies that each participant is present and active; the reason is optional and
     * normalised, with blank text stored as absent. The slot's own time rules are checked when the
     * slot is built.
     *
     * <p>Two rules are not covered here yet: an appointment must begin in the future — which needs
     * a clock this method does not receive — and it must not collide with another active
     * appointment, which the database enforces.
     *
     * @throws IllegalArgumentException if a participant is missing or inactive
     */
    public static Appointment book(
            Patient patient,
            Practitioner practitioner,
            Room examinationRoom,
            AppointmentSlot slot,
            String reason) {

        patient = requireActive(patient, "An appointment needs a patient");
        practitioner = requireActive(practitioner, "An appointment needs a practitioner");
        examinationRoom = requireActive(examinationRoom, "An appointment needs a room");
        reason = DomainText.normalizeOptional(reason);
        Objects.requireNonNull(slot);

        return new Appointment(patient, practitioner, examinationRoom, slot, reason);
    }

    /**
     * Returns the entity if it is present and active, so that a deactivated patient, practitioner
     * or room cannot be booked (spec §6.4).
     */
    private static <T extends Activatable> T requireActive(T entity, String nullErrorMessage) {
        if (entity == null) {
            throw new IllegalArgumentException(nullErrorMessage);
        }

        if (!entity.isActive()) {
            throw new IllegalArgumentException(
                    entity.getClass().getSimpleName() + " must be active to create an appointment");
        }

        return entity;
    }
}
