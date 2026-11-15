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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * REQ-F-11 and REQ-F-14: an owner permanently deletes a Training and no other
 * account can delete it.
 */
@SpringBootTest
@AutoConfigureMockMvc
class TrainingDeleteTest {

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
    void deletesTheTrainingPermanently() throws Exception {
        MockHttpSession session = registerAndLogin("runner@example.com");
        Training doomed = saveTraining(account("runner@example.com"),
                LocalDate.now().minusDays(3), 5200);

        mockMvc.perform(delete("/api/trainings/" + doomed.getId()).session(session))
                .andExpect(status().isNoContent());

        assertThat(trainings.findById(doomed.getId())).isEmpty();
    }

    @Test
    void returnsNotFoundForAnotherUsersTrainingAndLeavesItIntact() throws Exception {
        registerAndLogin("stranger@example.com");
        Training strangersTraining = saveTraining(account("stranger@example.com"),
                LocalDate.now().minusDays(3), 5200);
        MockHttpSession session = registerAndLogin("runner@example.com");

        mockMvc.perform(delete("/api/trainings/" + strangersTraining.getId()).session(session))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Training not found."));

        assertThat(trainings.findById(strangersTraining.getId())).isPresent();
    }

    @Test
    void returnsNotFoundForAnUnknownIdentifier() throws Exception {
        MockHttpSession session = registerAndLogin("runner@example.com");

        mockMvc.perform(delete("/api/trainings/999999").session(session))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Training not found."));
    }

    @Test
    void rejectsUnauthenticatedRequests() throws Exception {
        Training training = saveTraining(
                users.save(new UserAccount("runner@example.com", "hash")),
                LocalDate.now().minusDays(3), 5200);

        mockMvc.perform(delete("/api/trainings/" + training.getId()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Not authenticated."));

        assertThat(trainings.findById(training.getId())).isPresent();
    }
}
