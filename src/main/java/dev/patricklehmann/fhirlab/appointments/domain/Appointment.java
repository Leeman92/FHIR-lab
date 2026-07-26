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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name= "appointments")
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

    @Embedded
    private AppointmentSlot appointmentSlot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppointmentStatus status;

    private String reason;

    @CreationTimestamp
    private Instant createdAt;
    @UpdateTimestamp
    private Instant updatedAt;

    protected Appointment() {}

    public Appointment(
        Patient patient,
        Practitioner practitioner,
        Room examinationRoom,
        AppointmentSlot slot,
        String reason
    ) {
        this.status = AppointmentStatus.BOOKED;

        this.patient = requireActive(patient, "An appointment needs a patient");
        this.practitioner = requireActive(practitioner, "An appointment needs a practitioner");
        this.room = requireActive(examinationRoom, "An appointment needs a room");
        this.reason = DomainText.normalizeOptional(reason);
        this.appointmentSlot = Objects.requireNonNull(slot);
    }

    private <T extends Activatable> T requireActive(T entity, String nullErrorMessage) {
        if (entity == null) {
            throw new IllegalArgumentException(nullErrorMessage);
        }

        if (!entity.isActive()) {
            throw new IllegalArgumentException(entity.getClass().getSimpleName()+ " must be active to create an appointment");
        }

        return entity;
    }
}
