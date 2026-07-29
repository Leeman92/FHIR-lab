package dev.patricklehmann.fhirlab.patients.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.patricklehmann.fhirlab.patients.infrastructure.persistence.PatientRepository;
import dev.patricklehmann.fhirlab.support.PostgresIntegrationTest;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * End-to-end coverage of the patient endpoints against a real Postgres instance, organised by the
 * functional requirement each group verifies.
 */
class PatientControllerIT extends PostgresIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired PatientRepository patientRepository;

    /** Each test starts from an empty table so that counts and result sizes are exact. */
    @BeforeEach
    void clearPatients() {
        patientRepository.deleteAll();
    }

    /**
     * Builds a create-patient payload. Values are inserted unquoted when null so that a missing
     * field can be expressed as well as a blank one.
     */
    private String createPatientBody(String givenName, String familyName, LocalDate birthDate) {
        return """
                {
                  "name": {"givenName": %s, "familyName": %s},
                  "birthDate": %s
                }
                """
                .formatted(
                        quote(givenName),
                        quote(familyName),
                        quote(birthDate == null ? null : birthDate.toString()));
    }

    /** JSON-quotes a value, or renders {@code null} literally. */
    private String quote(String value) {
        return value == null ? "null" : "\"" + value + "\"";
    }

    /** Creates a patient and returns the parsed response body. */
    private JsonNode create(String givenName, String familyName, LocalDate birthDate)
            throws Exception {
        String response =
                mockMvc.perform(
                                post("/patients")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                createPatientBody(
                                                        givenName, familyName, birthDate)))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        return objectMapper.readTree(response);
    }

    @Nested
    @DisplayName("FR-001 – create patient")
    class CreatePatient {

        @Test
        @DisplayName("valid data creates an active patient with a unique id")
        void createsActivePatient() throws Exception {
            JsonNode created = create("Erika", "Mustermann", LocalDate.of(1985, 3, 12));

            assertThat(created.get("id").asText()).isNotBlank();
            assertThat(created.get("active").asBoolean()).isTrue();
            assertThat(created.get("createdAt").asText()).isNotBlank();
            assertThat(created.get("updatedAt").asText()).isNotBlank();
        }

        @Test
        @DisplayName("the created patient is retrievable by its id")
        void createdPatientIsRetrievable() throws Exception {
            String id = create("Erika", "Mustermann", LocalDate.of(1985, 3, 12)).get("id").asText();

            mockMvc.perform(get("/patients/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id))
                    .andExpect(jsonPath("$.patientName.givenName").value("Erika"))
                    .andExpect(jsonPath("$.patientName.familyName").value("Mustermann"))
                    .andExpect(jsonPath("$.birthDate").value("1985-03-12"));
        }

        @Test
        @DisplayName(
                "the response points at the new resource and creating twice yields two records")
        void createReturnsLocationAndIsNotIdempotent() throws Exception {
            mockMvc.perform(
                            post("/patients")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            createPatientBody(
                                                    "Klara", "Beispiel", LocalDate.of(1991, 1, 2))))
                    .andExpect(status().isCreated())
                    .andExpect(header().exists("Location"));

            // NFR-003 deliberately does not require creating the same patient twice to be
            // idempotent, and there is no natural key that could make it so.
            create("Klara", "Beispiel", LocalDate.of(1991, 1, 2));

            assertThat(patientRepository.count()).isEqualTo(2);
        }

        @Test
        @DisplayName("blank names are rejected and no record is stored")
        void rejectsBlankNames() throws Exception {
            mockMvc.perform(
                            post("/patients")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            createPatientBody("  ", "", LocalDate.of(1985, 3, 12))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors['name.givenName']").exists())
                    .andExpect(jsonPath("$.errors['name.familyName']").exists());

            assertThat(patientRepository.count()).isZero();
        }

        @Test
        @DisplayName("a name made of unicode whitespace is rejected by the request layer")
        void rejectsUnicodeWhitespaceName() throws Exception {
            // Two independent definitions of "blank" have to agree here: @NotBlank guards the
            // request, DomainText.strip() guards the domain. U+2003 EM SPACE survives String.trim()
            // but not String.strip(), so if @NotBlank ever loosened to trim() semantics this input
            // would reach the domain, throw IllegalArgumentException and surface as a 500.
            mockMvc.perform(
                            post("/patients")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            """
                                            {"name":{"givenName":"\\u2003",
                                                     "familyName":"Mustermann"},
                                             "birthDate":"1985-03-12"}
                                            """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors['name.givenName']").exists());

            assertThat(patientRepository.count()).isZero();
        }

        @Test
        @DisplayName("a future birth date is rejected and no record is stored")
        void rejectsFutureBirthDate() throws Exception {
            mockMvc.perform(
                            post("/patients")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            createPatientBody(
                                                    "Erika",
                                                    "Mustermann",
                                                    LocalDate.now().plusDays(1))))
                    .andExpect(status().isBadRequest());

            assertThat(patientRepository.count()).isZero();
        }

        @Test
        @Disabled(
                """
                FR-030 is not implemented yet. The birth-date rule lives in the domain and only \
                runs after bean validation has passed, so a request with both a blank name and a \
                future birth date reports the name errors and never mentions the birth date. \
                Enable this test once the future-date rule is also expressed as a field \
                constraint on the request.\
                """)
        @DisplayName("FR-030 – independent validation errors are reported together")
        void collectsAllValidationErrors() throws Exception {
            mockMvc.perform(
                            post("/patients")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            createPatientBody(
                                                    "", "", LocalDate.now().plusYears(1))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors['name.givenName']").exists())
                    .andExpect(jsonPath("$.errors['name.familyName']").exists())
                    .andExpect(jsonPath("$.errors.birthDate").exists());
        }
    }

    @Nested
    @DisplayName("FR-002 – retrieve patient")
    class GetPatient {

        @Test
        @DisplayName("an unknown id yields a not-found problem naming the requested id")
        void unknownIdYieldsNotFound() throws Exception {
            UUID unknownId = UUID.randomUUID();

            mockMvc.perform(get("/patients/{id}", unknownId))
                    .andExpect(status().isNotFound())
                    .andExpect(
                            content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.type").value("urn:problem:not-found"))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(
                            jsonPath("$.detail")
                                    .value(
                                            org.hamcrest.Matchers.containsString(
                                                    unknownId.toString())))
                    .andExpect(jsonPath("$.requestId").exists())
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("FR-029/NFR-004 – the request id is echoed back to the caller")
        void echoesRequestId() throws Exception {
            String requestId = UUID.randomUUID().toString();

            mockMvc.perform(
                            get("/patients/{id}", UUID.randomUUID())
                                    .header("X-Request-ID", requestId))
                    .andExpect(header().string("X-Request-ID", requestId))
                    .andExpect(jsonPath("$.requestId").value(requestId));
        }

        @Test
        @DisplayName("FR-032 – a malformed id does not leak internal details")
        void malformedIdIsRejectedCleanly() throws Exception {
            String body =
                    mockMvc.perform(get("/patients/{id}", "not-a-uuid"))
                            .andExpect(status().is4xxClientError())
                            .andReturn()
                            .getResponse()
                            .getContentAsString();

            assertThat(body)
                    .doesNotContain("java.")
                    .doesNotContain("dev.patricklehmann")
                    .doesNotContain("Exception");
        }
    }

    @Nested
    @DisplayName("FR-003 – search patients")
    class SearchPatients {

        @BeforeEach
        void seedPatients() throws Exception {
            create("Erika", "Mustermann", LocalDate.of(1985, 3, 12));
            create("Max", "Mustermann", LocalDate.of(1990, 7, 1));
            create("Hannelore", "Müller", LocalDate.of(1972, 11, 30));
        }

        @Test
        @DisplayName("a family name may match several patients")
        void findsMultiplePatients() throws Exception {
            mockMvc.perform(get("/patients").param("family", "Mustermann"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.foundPatients.length()").value(2));
        }

        @Test
        @DisplayName("search is case insensitive")
        void searchIsCaseInsensitive() throws Exception {
            mockMvc.perform(get("/patients").param("family", "MUSTERMANN"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.foundPatients.length()").value(2));
        }

        @Test
        @DisplayName("a part of a name is enough to find the patient")
        void findsByPartialName() throws Exception {
            mockMvc.perform(get("/patients").param("family", "Müll"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.foundPatients.length()").value(1))
                    .andExpect(
                            jsonPath("$.foundPatients[0].patientName.familyName").value("Müller"));
        }

        @Test
        @DisplayName("a short prefix of a long name is enough to find the patient")
        void findsByShortPrefix() throws Exception {
            mockMvc.perform(get("/patients").param("family", "Must"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.foundPatients.length()").value(2));
        }

        @Test
        @DisplayName("a part from the middle of a name is enough to find the patient")
        void findsByInfix() throws Exception {
            mockMvc.perform(get("/patients").param("family", "term"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.foundPatients.length()").value(2));

            mockMvc.perform(get("/patients").param("given", "nnel"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.foundPatients.length()").value(1))
                    .andExpect(
                            jsonPath("$.foundPatients[0].patientName.givenName")
                                    .value("Hannelore"));
        }

        @Test
        @DisplayName("an unrelated term does not match")
        void unrelatedTermDoesNotMatch() throws Exception {
            mockMvc.perform(get("/patients").param("family", "Schmidt"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.foundPatients.length()").value(0));
        }

        @Test
        @DisplayName("LIKE wildcards in the search term are treated as literal characters")
        void wildcardsAreEscaped() throws Exception {
            mockMvc.perform(get("/patients").param("family", "%"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.foundPatients.length()").value(0));

            mockMvc.perform(get("/patients").param("family", "M_ll"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.foundPatients.length()").value(0));
        }

        @Test
        @DisplayName("several criteria are combined with AND")
        void combinesCriteria() throws Exception {
            mockMvc.perform(get("/patients").param("given", "Max").param("family", "Mustermann"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.foundPatients.length()").value(1))
                    .andExpect(jsonPath("$.foundPatients[0].patientName.givenName").value("Max"));
        }

        @Test
        @DisplayName("a search without matches returns an empty result and no error")
        void emptyResultIsNotAnError() throws Exception {
            mockMvc.perform(get("/patients").param("family", "Nonexistent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.foundPatients").isArray())
                    .andExpect(jsonPath("$.foundPatients.length()").value(0));
        }

        @Test
        @DisplayName("the active status narrows the result")
        void filtersByActiveStatus() throws Exception {
            mockMvc.perform(get("/patients").param("family", "Mustermann").param("active", "false"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.foundPatients.length()").value(0));

            mockMvc.perform(get("/patients").param("family", "Mustermann").param("active", "true"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.foundPatients.length()").value(2));
        }
    }

    @Nested
    @DisplayName("FR-004 – deactivate patient")
    class DeactivatePatient {

        @Test
        @DisplayName("deactivating flips the active flag and keeps the patient retrievable")
        void deactivatesPatient() throws Exception {
            String id = create("Erika", "Mustermann", LocalDate.of(1985, 3, 12)).get("id").asText();

            mockMvc.perform(post("/patients/{id}/deactivate", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id))
                    .andExpect(jsonPath("$.active").value(false));

            mockMvc.perform(get("/patients/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.active").value(false))
                    .andExpect(jsonPath("$.patientName.givenName").value("Erika"))
                    .andExpect(jsonPath("$.birthDate").value("1985-03-12"));
        }

        @Test
        @DisplayName("deactivating twice does not change the state any further")
        void secondDeactivationIsANoOp() throws Exception {
            String id = create("Erika", "Mustermann", LocalDate.of(1985, 3, 12)).get("id").asText();

            String first =
                    mockMvc.perform(post("/patients/{id}/deactivate", id))
                            .andExpect(status().isOk())
                            .andReturn()
                            .getResponse()
                            .getContentAsString();

            String second =
                    mockMvc.perform(post("/patients/{id}/deactivate", id))
                            .andExpect(status().isOk())
                            .andReturn()
                            .getResponse()
                            .getContentAsString();

            assertThat(objectMapper.readTree(second)).isEqualTo(objectMapper.readTree(first));
        }

        @Test
        @DisplayName("a deactivated patient still shows up in an unfiltered search")
        void deactivatedPatientRemainsSearchable() throws Exception {
            String id = create("Erika", "Mustermann", LocalDate.of(1985, 3, 12)).get("id").asText();
            mockMvc.perform(post("/patients/{id}/deactivate", id)).andExpect(status().isOk());

            mockMvc.perform(get("/patients").param("family", "Mustermann"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.foundPatients.length()").value(1))
                    .andExpect(jsonPath("$.foundPatients[0].active").value(false));
        }

        @Test
        @DisplayName("deactivating an unknown patient yields a not-found problem")
        void unknownPatientYieldsNotFound() throws Exception {
            mockMvc.perform(post("/patients/{id}/deactivate", UUID.randomUUID()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.type").value("urn:problem:not-found"));
        }
    }
}
