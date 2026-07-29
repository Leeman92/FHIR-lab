package dev.patricklehmann.fhirlab.shared.domain;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Normalisation for free-text domain values.
 *
 * <p>Applies Unicode NFC, trims the ends and collapses internal whitespace runs, so that values
 * which a human would call identical are stored identically — which matters for equality, for
 * case-insensitive search, and for keeping visually blank input out of the database.
 */
public final class DomainText {

    private static final Pattern MULTIPLE_WHITESPACE = Pattern.compile("\\s+");

    /**
     * Normalises a mandatory value.
     *
     * <p>{@code fieldName} names the offending field in the exception message.
     *
     * @throws IllegalArgumentException if the value is null, or blank once normalised
     */
    public static String normalizeRequired(String value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFC).strip();
        normalized = MULTIPLE_WHITESPACE.matcher(normalized).replaceAll(" ");

        if (normalized.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }

        return normalized;
    }

    /**
     * Normalises an optional value, collapsing both null and blank input to null so that "absent"
     * has exactly one representation in the database.
     */
    public static String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFC).strip();
        normalized = MULTIPLE_WHITESPACE.matcher(normalized).replaceAll(" ");

        if (normalized.isBlank()) {
            return null;
        }

        return normalized;
    }
}
