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

/**
 * A physical treatment room an appointment takes place in (spec §6.3).
 *
 * <p>The label is mandatory and unique across the application; uniqueness is enforced by the {@code
 * uq_rooms_display_name} constraint rather than by a lookup, so it holds under concurrency. A
 * deactivated room stays visible and keeps existing appointments, but accepts no new ones.
 */
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

    /** Required by JPA; not for application use. */
    protected Room() {}

    /** Assumes a normalised argument — go through {@link #from} instead. */
    protected Room(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Builds a room with a normalised label.
     *
     * <p>As with {@code Practitioner.from}, the active flag is not set here, so the room is created
     * inactive and could not currently be booked.
     *
     * @throws IllegalArgumentException if the label is null or blank
     */
    public static Room from(String displayName) {
        displayName = DomainText.normalizeRequired(displayName, "Displayname cant be null");
        return new Room(displayName);
    }
}
