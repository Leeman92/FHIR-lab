package dev.patricklehmann.fhirlab.patients.domain;

import dev.patricklehmann.fhirlab.shared.domain.DomainText;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.util.Objects;

@Embeddable
@Getter
public class PatientName {

    @Column(name = "given_name", nullable = false, length = 100)
    private String givenName;

    @Column(name = "family_name", nullable = false, length = 100)
    private String familyName;

    protected PatientName() {
    }

    public PatientName(String givenName, String familyName) {
        this.givenName = DomainText.normalizeRequired(givenName, "Given name");
        this.familyName = DomainText.normalizeRequired(familyName, "Family name");
    }

    public String getDisplayName() {
        return givenName + " " + familyName;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof PatientName that)) {
            return false;
        }

        return givenName.equals(that.givenName)
            && familyName.equals(that.familyName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(givenName, familyName);
    }
}