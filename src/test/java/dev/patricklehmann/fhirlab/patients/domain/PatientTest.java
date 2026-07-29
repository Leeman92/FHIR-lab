package dev.patricklehmann.fhirlab.patients.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.patricklehmann.fhirlab.patients.application.exception.InvalidBirthdayException;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Unit tests for the patient invariants that do not need a database (NFR-007). */
class PatientTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 7, 29);

    /** Registers a valid patient with the given birth date, against a fixed "today". */
    private Patient register(LocalDate birthDate) {
        return Patient.register(PatientName.from("Erika", "Mustermann"), birthDate, TODAY);
    }

    @Test
    @DisplayName("a newly registered patient is active")
    void newPatientIsActive() {
        assertThat(register(LocalDate.of(1985, 3, 12)).isActive()).isTrue();
    }

    @Test
    @DisplayName("a birth date of today is accepted")
    void birthDateTodayIsAccepted() {
        assertThatCode(() -> register(TODAY)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("a birth date in the future is rejected")
    void futureBirthDateIsRejected() {
        assertThatThrownBy(() -> register(TODAY.plusDays(1)))
                .isInstanceOf(InvalidBirthdayException.class);
    }

    @Test
    @DisplayName("deactivation is idempotent")
    void deactivationIsIdempotent() {
        Patient patient = register(LocalDate.of(1985, 3, 12));

        patient.deactivate();
        assertThat(patient.isActive()).isFalse();

        patient.deactivate();
        assertThat(patient.isActive()).isFalse();
    }

    @Test
    @DisplayName("deactivation leaves the identifying data untouched")
    void deactivationKeepsData() {
        Patient patient = register(LocalDate.of(1985, 3, 12));

        patient.deactivate();

        assertThat(patient.getPatientName()).isEqualTo(PatientName.from("Erika", "Mustermann"));
        assertThat(patient.getBirthDate()).isEqualTo(LocalDate.of(1985, 3, 12));
    }

    @Test
    @DisplayName("names are normalised, so blank names never reach the database")
    void namesAreNormalised() {
        Patient patient =
                Patient.register(
                        PatientName.from("  Erika ", "van  der  Berg"),
                        LocalDate.of(1985, 3, 12),
                        TODAY);

        assertThat(patient.getPatientName().getGivenName()).isEqualTo("Erika");
        assertThat(patient.getPatientName().getFamilyName()).isEqualTo("van der Berg");

        assertThatThrownBy(() -> PatientName.from("   ", "Mustermann"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
