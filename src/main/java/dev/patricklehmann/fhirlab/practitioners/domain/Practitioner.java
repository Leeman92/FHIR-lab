package dev.patricklehmann.fhirlab.practitioners.domain;

import dev.patricklehmann.fhirlab.shared.domain.Activatable;
import dev.patricklehmann.fhirlab.shared.domain.DomainText;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name= "practitioners")
@Getter
public class Practitioner implements Activatable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Setter(AccessLevel.NONE)
    private UUID id;

    private String displayName;
    // Will be updated to a class/enum in the future
    private String speciality;
    private boolean active;

    @CreationTimestamp
    private Instant createdAt;
    @UpdateTimestamp
    private Instant updatedAt;

    protected Practitioner() {}

    protected Practitioner(String displayName, String speciality)
    {
        this.displayName = DomainText.normalizeRequired(displayName, "Displayname cant be null");
        this.speciality = DomainText.normalizeRequired(speciality, "Speciality must be non null");
    }
}
