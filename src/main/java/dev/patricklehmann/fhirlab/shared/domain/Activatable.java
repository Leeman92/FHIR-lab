package dev.patricklehmann.fhirlab.shared.domain;

@FunctionalInterface
public interface Activatable {
    boolean isActive();
}
