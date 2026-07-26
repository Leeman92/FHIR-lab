package dev.patricklehmann.fhirlab.rooms.domain;

import dev.patricklehmann.fhirlab.shared.domain.Activatable;
import dev.patricklehmann.fhirlab.shared.domain.DomainText;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "rooms")
@Getter
public class Room implements Activatable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Setter(AccessLevel.NONE)
    private UUID id;

    @Column(name = "display_name", nullable = false, length = 200)
    private String displayName;

    private boolean active;

    @CreationTimestamp private Instant createdAt;
    @UpdateTimestamp private Instant updatedAt;

    protected Room() {}

    protected Room(String displayName) {
        this.displayName = displayName;
    }

    public static Room from(String displayName) {
        displayName = DomainText.normalizeRequired(displayName, "Displayname cant be null");
        return new Room(displayName);
    }
}
