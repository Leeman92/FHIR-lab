package dev.patricklehmann.fhirlab.patients.infrastructure.persistence;

import dev.patricklehmann.fhirlab.patients.domain.Patient;
import jakarta.persistence.criteria.Path;
import java.time.LocalDate;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

/**
 * Reusable predicates for the patient search (FR-003).
 *
 * <p>Each factory returns one independent criterion; the caller combines whichever apply, which is
 * what makes arbitrary combinations possible without a query per permutation.
 */
public final class PatientSpecifications {

    /**
     * Escape character for the {@code LIKE} patterns below. Search terms are user input and may
     * legitimately contain {@code %} or {@code _}; without escaping, those would silently act as
     * wildcards.
     */
    private static final char LIKE_ESCAPE = '\\';

    /** Utility class. */
    private PatientSpecifications() {}

    /** Matches any part of the given name, ignoring case. */
    public static Specification<Patient> givenNameContaining(String givenName) {
        return nameContaining("givenName", givenName);
    }

    /** Matches any part of the family name, ignoring case. */
    public static Specification<Patient> familyNameContaining(String familyName) {
        return nameContaining("familyName", familyName);
    }

    /** Matches an exact birth date. */
    public static Specification<Patient> hasBirthDate(LocalDate birthDate) {
        return (root, query, builder) -> builder.equal(root.get("birthDate"), birthDate);
    }

    /** Restricts to active or inactive patients; omit the criterion to get both. */
    public static Specification<Patient> hasActiveStatus(boolean active) {
        return (root, query, builder) -> builder.equal(root.get("active"), active);
    }

    /**
     * Matches patients whose name contains the search term, ignoring case (FR-003).
     *
     * <p>Containment is deliberate: the requirement is to find a patient by any part of a name,
     * including a segment from the middle. A prefix comparison would miss those, and a trigram
     * similarity threshold misses them too — {@code word_similarity('term', 'mustermann')} is only
     * 0.4, below any threshold that still excludes unrelated names. The trigram GIN indexes from
     * {@code V5__patient_name_infix_search.sql} keep this pattern indexable.
     */
    private static Specification<Patient> nameContaining(String nameAttribute, String searchTerm) {
        String pattern = "%" + escapeLikeWildcards(normalize(searchTerm)) + "%";

        return (root, query, builder) -> {
            Path<String> name = root.get("patientName").get(nameAttribute);

            return builder.like(builder.lower(name), pattern, LIKE_ESCAPE);
        };
    }

    /** Neutralises {@code %}, {@code _} and the escape character itself in user input. */
    private static String escapeLikeWildcards(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    /** Trims and lower-cases, matching the {@code lower(...)} applied to the column. */
    private static String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
