package com.runningtracker.training;

import com.runningtracker.domain.Training;
import com.runningtracker.domain.UserAccount;
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

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * REQ-F-8 and REQ-F-12: history is the full set of the account's Trainings,
 * grouped by Month, newest Month first and newest Training first, with each
 * group headed by that Month's total distance.
 */
@SpringBootTest
@AutoConfigureMockMvc
class TrainingHistoryTest {

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

    private Training saveTraining(UserAccount owner, LocalDate date, int distanceMetres) {
        return trainings.save(new Training(date, distanceMetres, 1800, owner));
    }

    @Test
    void groupsTrainingsByMonthNewestFirstWithMonthTotals() throws Exception {
        MockHttpSession session = registerAndLogin("runner@example.com");
        UserAccount owner = account("runner@example.com");

        saveTraining(owner, LocalDate.of(2026, 11, 1), 4800);
        saveTraining(owner, LocalDate.of(2026, 11, 20), 5200);
        saveTraining(owner, LocalDate.of(2026, 10, 28), 7500);

        mockMvc.perform(get("/api/trainings").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.months", hasSize(2)))
                .andExpect(jsonPath("$.months[0].month").value("2026-11"))
                .andExpect(jsonPath("$.months[0].totalDistanceKm").value(10.0))
                .andExpect(jsonPath("$.months[0].trainings", hasSize(2)))
                .andExpect(jsonPath("$.months[0].trainings[0].date").value("2026-11-20"))
                .andExpect(jsonPath("$.months[0].trainings[1].date").value("2026-11-01"))
                .andExpect(jsonPath("$.months[1].month").value("2026-10"))
                .andExpect(jsonPath("$.months[1].totalDistanceKm").value(7.5))
                .andExpect(jsonPath("$.months[1].trainings", hasSize(1)));
    }

    @Test
    void returnsAnEmptyHistoryWhenNoTrainingsExist() throws Exception {
        MockHttpSession session = registerAndLogin("runner@example.com");

        mockMvc.perform(get("/api/trainings").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.months").isEmpty());
    }

    @Test
    void excludesOtherUsersTrainings() throws Exception {
        registerAndLogin("stranger@example.com");
        saveTraining(account("stranger@example.com"), LocalDate.of(2026, 11, 3), 5200);

        MockHttpSession session = registerAndLogin("runner@example.com");
        saveTraining(account("runner@example.com"), LocalDate.of(2026, 10, 1), 1000);

        mockMvc.perform(get("/api/trainings").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.months", hasSize(1)))
                .andExpect(jsonPath("$.months[0].month").value("2026-10"))
                .andExpect(jsonPath("$.months[0].totalDistanceKm").value(1.0))
                .andExpect(jsonPath("$.months[0].trainings", hasSize(1)));
    }

    @Test
    void returnsEveryTrainingWithoutPagination() throws Exception {
        MockHttpSession session = registerAndLogin("runner@example.com");
        UserAccount owner = account("runner@example.com");

        for (int day = 1; day <= 30; day++) {
            saveTraining(owner, LocalDate.of(2026, 9, day), 1000);
        }

        mockMvc.perform(get("/api/trainings").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.months", hasSize(1)))
                .andExpect(jsonPath("$.months[0].trainings", hasSize(30)))
                .andExpect(jsonPath("$.months[0].totalDistanceKm").value(30.0));
    }

    @Test
    void rejectsUnauthenticatedRequests() throws Exception {
        mockMvc.perform(get("/api/trainings"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Not authenticated."));
    }
}
