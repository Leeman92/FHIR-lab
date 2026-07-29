package dev.patricklehmann.fhirlab.appointments.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;

/**
 * The period an appointment occupies, as a half-open interval (spec §6.4).
 *
 * <p>Both ends are stored as instants so the period is unambiguous regardless of local
 * representation (NFR-005). Treating the interval as half-open — start inclusive, end exclusive —
 * is what makes back-to-back appointments legal: a period ending at 10:30 does not collide with one
 * starting at 10:30 (FR-013). The schema's {@code tstzrange(start, end, '[)')} matches this
 * exactly.
 */
@Embeddable
@Getter
public class AppointmentSlot {

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    /** Required by JPA; not for application use. */
    public AppointmentSlot() {}

    /** How long this period lasts. */
    protected Duration getAppointmentDuration() {
        return Duration.between(startTime, endTime);
    }

    /** Assumes validated arguments — go through {@link #reserve} instead. */
    protected AppointmentSlot(Instant startTime, Instant endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * Builds a period that satisfies every time rule in spec §6.4: the end lies after the start,
     * both ends fall on a full five minutes, and the duration is between 15 and 180 minutes
     * inclusive.
     *
     * <p>The five-minute grid means no duration can carry a fractional minute, so comparing whole
     * minutes below is exact.
     *
     * @throws IllegalArgumentException if any of those rules is violated
     */
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

    /**
     * True unless the instant sits exactly on a five-minute mark, sub-second precision included.
     */
    private static boolean isNotOnFiveMinuteBoundary(Instant instant) {
        return instant.getEpochSecond() % 300 != 0 || instant.getNano() != 0;
    }
}
