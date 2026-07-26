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

    public Appointment() {}

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
