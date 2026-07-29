package dev.patricklehmann.fhirlab.appointments.domain;

/**
 * The lifecycle of an appointment (spec §6.4).
 *
 * <p>These are the project's own status values, kept separate from FHIR's on purpose: the mapping
 * onto the FHIR {@code Appointment} status is declared in one place and tested there (FR-025,
 * FR-028), so neither vocabulary constrains the other.
 *
 * <p>Only {@link #CANCELLED} frees the period again; a completed or missed appointment still
 * occupies its slot in the record and can no longer be moved.
 */
public enum AppointmentStatus {
    BOOKED,
    CANCELLED,
    COMPLETED,
    NO_SHOW,
}
