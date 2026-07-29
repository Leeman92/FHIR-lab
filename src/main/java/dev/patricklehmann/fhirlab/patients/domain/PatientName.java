package dev.patricklehmann.fhirlab.patients.domain;

import dev.patricklehmann.fhirlab.shared.domain.DomainText;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import lombok.Getter;

/**
 * A patient's given and family name (spec §6.1).
 *
 * <p>Both parts are mandatory and normalised — Unicode NFC, trimmed, runs of whitespace collapsed —
 * so that equal names compare equal and searches see consistent data. Build one through {@link
 * #from}; the constructor performs no validation and exists for JPA.
 *
 * <p>Value semantics: two names with the same parts are equal, which is what makes this usable as
 * an embeddable rather than an entity.
 */
@Embeddable
@Getter
public class PatientName {

    @Column(name = "given_name", nullable = false, length = 100)
    private String givenName;

    @Column(name = "family_name", nullable = false, length = 100)
    private String familyName;

    /** Required by JPA; not for application use. */
    protected PatientName() {}

    /** Assumes normalised arguments — go through {@link #from} instead. */
    protected PatientName(String givenName, String familyName) {
        this.givenName = givenName;
        this.familyName = familyName;
    }

    /**
     * Builds a normalised name.
     *
     * @throws IllegalArgumentException if either part is null, empty or whitespace-only
     */
    public static PatientName from(String givenName, String familyName) {
        givenName = DomainText.normalizeRequired(givenName, "Given name");
        familyName = DomainText.normalizeRequired(familyName, "Family name");

        return new PatientName(givenName, familyName);
    }

    /** The name as a single string, for display and for FHIR's {@code name.text}. */
    public String getDisplayName() {
        return givenName.trim() + " " + familyName.trim();
    }

    @Override
    /** Value equality over both name parts; both are normalised, so equal names compare equal. */
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof PatientName that)) {
            return false;
        }

        return givenName.equals(that.givenName) && familyName.equals(that.familyName);
    }

    @Override
    /** Consistent with {@link #equals}. */
    public int hashCode() {
        return Objects.hash(givenName, familyName);
    }
}
