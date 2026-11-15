package com.runningtracker.training;

import com.runningtracker.domain.Training;
import com.runningtracker.repository.TrainingRepository;
import com.runningtracker.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * REQ-F-4: an authenticated user records a Training through
 * {@code POST /api/trainings}, validated by the ticket 5 rules and owned by
 * the session's account.
 */
@SpringBootTest
@AutoConfigureMockMvc
class TrainingControllerTest {

    private static final String EMAIL = "runner@example.com";
    private static final String PASSWORD = "correct-horse-battery";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TrainingRepository trainings;

    @Autowired
    private UserAccountRepository users;

    @BeforeEach
    void cleanDatabase() {
        trainings.deleteAll();
        users.deleteAll();
    }

    private MockHttpSession authenticatedSession() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + EMAIL + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isNoContent());

        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + EMAIL + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andReturn();

        return (MockHttpSession) login.getRequest().getSession(false);
    }

    private org.springframework.test.web.servlet.ResultActions createTraining(MockHttpSession session,
                                                                            String date,
                                                                            String distanceKm,
                                                                            String durationSeconds) throws Exception {
        return mockMvc.perform(post("/api/trainings")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"date\":\"" + date + "\","
                        + "\"distanceKm\":" + distanceKm + ","
                        + "\"durationSeconds\":" + durationSeconds + "}"));
    }

    @Test
    void createsATrainingForTheAuthenticatedAccount() throws Exception {
        MockHttpSession session = authenticatedSession();
        LocalDate date = LocalDate.now().minusDays(1);

        createTraining(session, date.toString(), "5.2", "1800")
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/api/trainings/\\d+")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.date").value(date.toString()))
                .andExpect(jsonPath("$.distanceKm").value(5.2))
                .andExpect(jsonPath("$.durationSeconds").value(1800));

        Training saved = trainings.findAllByOwnerEmail(EMAIL).getFirst();
        assertThat(saved.getDate()).isEqualTo(date);
        assertThat(saved.getDistanceMetres()).isEqualTo(5200);
        assertThat(saved.getDurationSeconds()).isEqualTo(1800);
    }

    @Test
    void normalisesDistanceToWholeMetres() throws Exception {
        MockHttpSession session = authenticatedSession();

        createTraining(session, LocalDate.now().toString(), "5.239", "1800")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.distanceKm").value(5.23));

        assertThat(trainings.findAllByOwnerEmail(EMAIL).getFirst().getDistanceMetres()).isEqualTo(5230);
    }

    @Test
    void acceptsTheOneDayDateToleranceAndTheMinimumValues() throws Exception {
        MockHttpSession session = authenticatedSession();

        createTraining(session, LocalDate.now().plusDays(1).toString(), "0.01", "1")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.distanceKm").value(0.01))
                .andExpect(jsonPath("$.durationSeconds").value(1));
    }

    @Test
    void rejectsADateLaterThanTomorrow() throws Exception {
        MockHttpSession session = authenticatedSession();

        createTraining(session, LocalDate.now().plusDays(2).toString(), "5.2", "1800")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Date must not be later than tomorrow."));

        assertThat(trainings.count()).isZero();
    }

    @Test
    void rejectsADistanceBelowTheMinimum() throws Exception {
        MockHttpSession session = authenticatedSession();

        createTraining(session, LocalDate.now().toString(), "0.001", "1800")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Distance must be between 0.01 and 100 km."));

        assertThat(trainings.count()).isZero();
    }

    @Test
    void rejectsADistanceAboveTheMaximum() throws Exception {
        MockHttpSession session = authenticatedSession();

        createTraining(session, LocalDate.now().toString(), "100.01", "1800")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Distance must be between 0.01 and 100 km."));

        assertThat(trainings.count()).isZero();
    }

    @Test
    void rejectsAZeroDuration() throws Exception {
        MockHttpSession session = authenticatedSession();

        createTraining(session, LocalDate.now().toString(), "5.2", "0")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Duration must be greater than zero."));

        assertThat(trainings.count()).isZero();
    }

    @Test
    void rejectsUnauthenticatedRequests() throws Exception {
        mockMvc.perform(post("/api/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"date\":\"" + LocalDate.now() + "\",\"distanceKm\":5.2,\"durationSeconds\":1800}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Not authenticated."));

        assertThat(trainings.count()).isZero();
    }
}
