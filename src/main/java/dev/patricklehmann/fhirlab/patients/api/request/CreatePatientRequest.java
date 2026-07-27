package dev.patricklehmann.fhirlab.patients.api.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreatePatientRequest(
        @NotNull @Valid PatientNameRequest name, @NotNull LocalDate birthDate) {}
