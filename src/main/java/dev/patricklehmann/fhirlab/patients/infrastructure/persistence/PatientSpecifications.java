package dev.patricklehmann.fhirlab.patients.infrastructure.persistence;

import dev.patricklehmann.fhirlab.patients.domain.Patient;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Locale;

public final class PatientSpecifications {

    private static final double NAME_SIMILARITY_THRESHOLD = 0.5;

    private PatientSpecifications() {
    }

    public static Specification<Patient> givenNameSimilarTo(String givenName) {
        return wordSimilarTo(
            "givenName",
            normalize(givenName),
            NAME_SIMILARITY_THRESHOLD
        );
    }

    public static Specification<Patient> familyNameSimilarTo(String familyName) {
        return wordSimilarTo(
            "familyName",
            normalize(familyName),
            NAME_SIMILARITY_THRESHOLD
        );
    }

    public static Specification<Patient> hasBirthDate(LocalDate birthDate) {
        return (root, query, builder) ->
            builder.equal(root.get("birthDate"), birthDate);
    }

    public static Specification<Patient> hasActiveStatus(boolean active) {
        return (root, query, builder) ->
            builder.equal(root.get("active"), active);
    }

    private static Specification<Patient> wordSimilarTo(
        String nameAttribute,
        String searchTerm,
        double threshold
    ) {
        return (root, query, builder) -> {
            Path<String> name = root
                .get("patientName")
                .get(nameAttribute);

            Expression<Double> similarity = builder.function(
                "word_similarity",
                Double.class,
                builder.literal(searchTerm),
                builder.lower(name)
            );

            return builder.greaterThanOrEqualTo(
                similarity,
                threshold
            );
        };
    }

    private static String normalize(String value) {
        return value
            .trim()
            .toLowerCase(Locale.ROOT);
    }
}