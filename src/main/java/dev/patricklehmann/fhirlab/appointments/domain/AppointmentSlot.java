package dev.patricklehmann.fhirlab.appointments.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;

@Embeddable
@Getter
public class AppointmentSlot {

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    public AppointmentSlot() {}

    protected Duration getAppointmentDuration() {
        return Duration.between(startTime, endTime);
    }

    protected AppointmentSlot(Instant startTime, Instant endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public AppointmentSlot reserve(Instant startTime, Instant endTime) {
        Objects.requireNonNull(startTime, "Start time must not be null");
        Objects.requireNonNull(endTime, "End time must not be null");

        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException(
                    "The appointments end time must be after the start time");
        }

        if (isNotOnFiveMinuteBoundary(startTime)) {
            throw new IllegalArgumentException(
                    "The start time must fall on a full five-minute boundary");
        }

        if (isNotOnFiveMinuteBoundary(endTime)) {
            throw new IllegalArgumentException(
                    "The end time must fall on a full five-minute boundary");
        }

        Duration appointmentDuration = Duration.between(startTime, endTime);
        if (appointmentDuration.toMinutes() < 15 || appointmentDuration.toMinutes() > 180) {
            throw new IllegalArgumentException(
                    "The duration of the appointment must be between 15 Minutes and 3 hours");
        }

        return new AppointmentSlot(startTime, endTime);
    }

    private static boolean isNotOnFiveMinuteBoundary(Instant instant) {
        return instant.getEpochSecond() % 300 != 0 || instant.getNano() != 0;
    }
}
