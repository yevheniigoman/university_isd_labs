package com.runningtracker.training;

import com.runningtracker.domain.Training;
import com.runningtracker.domain.UserAccount;
import com.runningtracker.repository.TrainingRepository;
import com.runningtracker.repository.MonthlyGoalRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * REQ-F-9 and REQ-F-14: a Training's details are returned to its owner and to
 * nobody else, with a cross-account identifier treated as not found.
 */
@SpringBootTest
@AutoConfigureMockMvc
class TrainingDetailsTest {

    private static final String PASSWORD = "correct-horse-battery";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TrainingRepository trainings;

    @Autowired
    private MonthlyGoalRepository goals;

    @Autowired
    private UserAccountRepository users;

    @BeforeEach
    void cleanDatabase() {
        trainings.deleteAll();
        goals.deleteAll();
        users.deleteAll();
    }

    private MockHttpSession registerAndLogin(String email) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isNoContent());

        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andReturn();

        return (MockHttpSession) login.getRequest().getSession(false);
    }

    private UserAccount account(String email) {
        return users.findByEmail(email).orElseThrow();
    }

    private Training saveTraining(UserAccount owner, LocalDate date, int distanceMetres, int durationSeconds) {
        return trainings.save(new Training(date, distanceMetres, durationSeconds, owner));
    }

    @Test
    void returnsTheRequestedTrainingToItsOwner() throws Exception {
        MockHttpSession session = registerAndLogin("runner@example.com");
        Training training = saveTraining(account("runner@example.com"),
                LocalDate.of(2026, 11, 3), 5200, 1800);

        mockMvc.perform(get("/api/trainings/" + training.getId()).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(training.getId().intValue()))
                .andExpect(jsonPath("$.date").value("2026-11-03"))
                .andExpect(jsonPath("$.distanceKm").value(5.2))
                .andExpect(jsonPath("$.durationSeconds").value(1800));
    }

    @Test
    void returnsNotFoundForAnotherUsersTraining() throws Exception {
        registerAndLogin("stranger@example.com");
        Training strangersTraining = saveTraining(account("stranger@example.com"),
                LocalDate.of(2026, 11, 3), 5200, 1800);
        MockHttpSession session = registerAndLogin("runner@example.com");

        mockMvc.perform(get("/api/trainings/" + strangersTraining.getId()).session(session))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Training not found."));
    }

    @Test
    void returnsNotFoundForAnUnknownIdentifier() throws Exception {
        MockHttpSession session = registerAndLogin("runner@example.com");

        mockMvc.perform(get("/api/trainings/999999").session(session))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Training not found."));
    }

    @Test
    void rejectsUnauthenticatedRequests() throws Exception {
        Training training = saveTraining(
                users.save(new UserAccount("runner@example.com", "hash")),
                LocalDate.of(2026, 11, 3), 5200, 1800);

        mockMvc.perform(get("/api/trainings/" + training.getId()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Not authenticated."));
    }
}
