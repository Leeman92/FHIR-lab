package dev.patricklehmann.fhirlab.shared.domain;

/**
 * A record that can be deactivated without being deleted.
 *
 * <p>Patients, practitioners and rooms all share this lifecycle: a deactivated record stays visible
 * and keeps its existing appointments, but cannot be used for new ones. Letting them share one type
 * is what allows {@code Appointment.book} to check all three participants uniformly.
 */
@FunctionalInterface
public interface Activatable {
    /** False once the record has been deactivated; new appointments must then be refused. */
    boolean isActive();
}
