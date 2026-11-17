package com.runningtracker.goal;

import com.runningtracker.domain.Training;
import com.runningtracker.domain.UserAccount;
import com.runningtracker.repository.MonthlyGoalRepository;
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
import java.time.YearMonth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * REQ-F-15: Completed distance is the Month total distance of the Goal Month,
 * recomputed from the current Trainings on every request.
 */
@SpringBootTest
@AutoConfigureMockMvc
class GoalProgressTest {

    private static final String PASSWORD = "correct-horse-battery";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MonthlyGoalRepository goals;

    @Autowired
    private TrainingRepository trainings;

    @Autowired
    private UserAccountRepository users;

    @BeforeEach
    void cleanDatabase() {
        goals.deleteAll();
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

    private void setGoal(MockHttpSession session, String month, String targetKm) throws Exception {
        mockMvc.perform(put("/api/goals/" + month)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetDistanceKm\":" + targetKm + "}"))
                .andExpect(status().isOk());
    }

    private void expectProgress(MockHttpSession session, String month, double completed,
                                double remaining, double percent) throws Exception {
        mockMvc.perform(get("/api/goals/" + month).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedDistanceKm").value(completed))
                .andExpect(jsonPath("$.remainingDistanceKm").value(remaining))
                .andExpect(jsonPath("$.percentComplete").value(percent));
    }

    @Test
    void reportsProgressFromTrainingsInTheGoalMonth() throws Exception {
        MockHttpSession session = registerAndLogin("runner@example.com");
        UserAccount owner = account("runner@example.com");
        YearMonth month = YearMonth.now();

        saveTraining(owner, month.atDay(3), 12_300);
        saveTraining(owner, month.atDay(10), 20_200);
        setGoal(session, month.toString(), "50");

        expectProgress(session, month.toString(), 32.5, 17.5, 65.0);
    }

    @Test
    void ignoresTrainingsInOtherMonths() throws Exception {
        MockHttpSession session = registerAndLogin("runner@example.com");
        UserAccount owner = account("runner@example.com");
        YearMonth month = YearMonth.now();

        saveTraining(owner, month.minusMonths(1).atDay(1), 12_300);
        saveTraining(owner, month.plusMonths(1).atDay(1), 20_200);
        setGoal(session, month.toString(), "50");

        expectProgress(session, month.toString(), 0.0, 50.0, 0.0);
    }

    @Test
    void floorsRemainingAtZeroAndLeavesPercentUncapped() throws Exception {
        MockHttpSession session = registerAndLogin("runner@example.com");
        UserAccount owner = account("runner@example.com");
        YearMonth month = YearMonth.now();

        saveTraining(owner, month.atDay(1), 25_000);
        setGoal(session, month.toString(), "20");

        expectProgress(session, month.toString(), 25.0, 0.0, 125.0);
    }

    @Test
    void roundsPercentToOneDecimalPlace() throws Exception {
        MockHttpSession session = registerAndLogin("runner@example.com");
        UserAccount owner = account("runner@example.com");
        YearMonth month = YearMonth.now();

        saveTraining(owner, month.atDay(1), 10_000);
        setGoal(session, month.toString(), "30");

        expectProgress(session, month.toString(), 10.0, 20.0, 33.3);
    }

    @Test
    void recomputesProgressAfterATrainingIsCreated() throws Exception {
        MockHttpSession session = registerAndLogin("runner@example.com");
        UserAccount owner = account("runner@example.com");
        YearMonth month = YearMonth.now();

        setGoal(session, month.toString(), "50");
        expectProgress(session, month.toString(), 0.0, 50.0, 0.0);

        saveTraining(owner, month.atDay(1), 5_200);

        expectProgress(session, month.toString(), 5.2, 44.8, 10.4);
    }

    @Test
    void recomputesProgressAfterATrainingIsDeleted() throws Exception {
        MockHttpSession session = registerAndLogin("runner@example.com");
        UserAccount owner = account("runner@example.com");
        YearMonth month = YearMonth.now();

        Training training = saveTraining(owner, month.atDay(1), 5_200);
        setGoal(session, month.toString(), "50");
        expectProgress(session, month.toString(), 5.2, 44.8, 10.4);

        trainings.delete(training);

        expectProgress(session, month.toString(), 0.0, 50.0, 0.0);
    }

    @Test
    void recomputesProgressAfterATrainingIsReDatedAcrossMonths() throws Exception {
        MockHttpSession session = registerAndLogin("runner@example.com");
        UserAccount owner = account("runner@example.com");
        YearMonth current = YearMonth.now();
        YearMonth previous = current.minusMonths(1);

        Training training = saveTraining(owner, previous.atDay(1), 5_200);
        setGoal(session, previous.toString(), "50");
        setGoal(session, current.toString(), "50");

        expectProgress(session, previous.toString(), 5.2, 44.8, 10.4);
        expectProgress(session, current.toString(), 0.0, 50.0, 0.0);

        training.update(current.atDay(1), 5_200, 1800);
        trainings.save(training);

        expectProgress(session, previous.toString(), 0.0, 50.0, 0.0);
        expectProgress(session, current.toString(), 5.2, 44.8, 10.4);
    }

    @Test
    void usesOnlyTheAuthenticatedUsersTrainings() throws Exception {
        YearMonth month = YearMonth.now();

        MockHttpSession strangersSession = registerAndLogin("stranger@example.com");
        saveTraining(account("stranger@example.com"), month.atDay(1), 99_000);

        MockHttpSession session = registerAndLogin("runner@example.com");
        saveTraining(account("runner@example.com"), month.atDay(2), 5_200);
        setGoal(session, month.toString(), "50");

        expectProgress(session, month.toString(), 5.2, 44.8, 10.4);

        // The stranger's own progress is unaffected by the runner's data.
        setGoal(strangersSession, month.toString(), "100");
        expectProgress(strangersSession, month.toString(), 99.0, 1.0, 99.0);
    }
}
