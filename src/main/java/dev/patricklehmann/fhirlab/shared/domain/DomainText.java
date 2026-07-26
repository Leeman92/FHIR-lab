package dev.patricklehmann.fhirlab.shared.domain;

import java.text.Normalizer;
import java.util.regex.Pattern;

public final class DomainText {

    private static final Pattern MULTIPLE_WHITESPACE = Pattern.compile("\\s+");

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