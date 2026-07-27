package dev.patricklehmann.fhirlab.patients.domain;

import dev.patricklehmann.fhirlab.shared.domain.DomainText;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import lombok.Getter;

@Embeddable
@Getter
public class PatientName {

    @Column(name = "given_name", nullable = false, length = 100)
    private String givenName;

    @Column(name = "family_name", nullable = false, length = 100)
    private String familyName;

    protected PatientName() {}

    protected PatientName(String givenName, String familyName) {
        this.givenName = givenName;
        this.familyName = familyName;
    }

    public static PatientName from(String givenName, String familyName) {
        givenName = DomainText.normalizeRequired(givenName, "Given name");
        familyName = DomainText.normalizeRequired(familyName, "Family name");

        return new PatientName(givenName, familyName);
    }

    public String getDisplayName() {
        return givenName.trim() + " " + familyName.trim();
    }

    @Override
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
    public int hashCode() {
        return Objects.hash(givenName, familyName);
    }
}
