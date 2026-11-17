package com.runningtracker.goal;

import com.runningtracker.domain.MonthlyGoal;
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
import org.springframework.test.web.servlet.ResultActions;

import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * REQ-F-15: setting, replacing, reading, and removing a Monthly Goal, with the
 * documented validation and the account isolation of REQ-F-14.
 */
@SpringBootTest
@AutoConfigureMockMvc
class GoalCrudTest {

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

    private ResultActions setGoal(MockHttpSession session, String month, String targetKm) throws Exception {
        return mockMvc.perform(put("/api/goals/" + month)
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"targetDistanceKm\":" + targetKm + "}"));
    }

    private MonthlyGoal goal(String email, String month) {
        return goals.findByOwnerEmailAndGoalMonth(email, YearMonth.parse(month)).orElseThrow();
    }

    @Test
    void createsAGoalAndReportsZeroProgress() throws Exception {
        MockHttpSession session = registerAndLogin("runner@example.com");

        setGoal(session, "2026-11", "50")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").value("2026-11"))
                .andExpect(jsonPath("$.targetDistanceKm").value(50.0))
                .andExpect(jsonPath("$.completedDistanceKm").value(0.0))
                .andExpect(jsonPath("$.remainingDistanceKm").value(50.0))
                .andExpect(jsonPath("$.percentComplete").value(0.0));

        assertThat(goals.count()).isEqualTo(1);
        assertThat(goal("runner@example.com", "2026-11").getTargetMetres()).isEqualTo(50_000);
    }

    @Test
    void replacesAnExistingGoalForTheSameMonth() throws Exception {
        MockHttpSession session = registerAndLogin("runner@example.com");

        setGoal(session, "2026-11", "50").andExpect(status().isOk());
        setGoal(session, "2026-11", "60")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetDistanceKm").value(60.0));

        assertThat(goals.count()).isEqualTo(1);
        assertThat(goal("runner@example.com", "2026-11").getTargetMetres()).isEqualTo(60_000);
    }

    @Test
    void truncatesATargetBeyondTwoDecimalPlaces() throws Exception {
        MockHttpSession session = registerAndLogin("runner@example.com");

        setGoal(session, "2026-11", "50.239")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetDistanceKm").value(50.23));

        assertThat(goal("runner@example.com", "2026-11").getTargetMetres()).isEqualTo(50_230);
    }

    @Test
    void rejectsAnOutOfRangeTargetAndLeavesTheGoalUnchanged() throws Exception {
        MockHttpSession session = registerAndLogin("runner@example.com");
        setGoal(session, "2026-11", "50").andExpect(status().isOk());

        setGoal(session, "2026-11", "1000.01")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Target distance must be between 0.01 and 1000 km."));

        setGoal(session, "2026-11", "0.009")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Target distance must be between 0.01 and 1000 km."));

        assertThat(goal("runner@example.com", "2026-11").getTargetMetres()).isEqualTo(50_000);
    }

    @Test
    void rejectsAMalformedMonth() throws Exception {
        MockHttpSession session = registerAndLogin("runner@example.com");

        setGoal(session, "2026-13", "50")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Month must be a valid year and month (YYYY-MM)."));

        assertThat(goals.count()).isZero();
    }

    @Test
    void returnsNotFoundForAnAbsentGoal() throws Exception {
        MockHttpSession session = registerAndLogin("runner@example.com");

        mockMvc.perform(get("/api/goals/2026-11").session(session))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Goal not found."));
    }

    @Test
    void deletesAGoalPermanently() throws Exception {
        MockHttpSession session = registerAndLogin("runner@example.com");
        setGoal(session, "2026-11", "50").andExpect(status().isOk());

        mockMvc.perform(delete("/api/goals/2026-11").session(session))
                .andExpect(status().isNoContent());

        assertThat(goals.count()).isZero();

        mockMvc.perform(get("/api/goals/2026-11").session(session))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnsNotFoundWhenDeletingAnAbsentGoal() throws Exception {
        MockHttpSession session = registerAndLogin("runner@example.com");

        mockMvc.perform(delete("/api/goals/2026-11").session(session))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Goal not found."));
    }

    @Test
    void returnsNotFoundForAnotherAccountsGoal() throws Exception {
        MockHttpSession strangersSession = registerAndLogin("stranger@example.com");
        setGoal(strangersSession, "2026-11", "50").andExpect(status().isOk());

        MockHttpSession session = registerAndLogin("runner@example.com");

        mockMvc.perform(get("/api/goals/2026-11").session(session))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Goal not found."));

        mockMvc.perform(delete("/api/goals/2026-11").session(session))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Goal not found."));

        assertThat(goals.count()).isEqualTo(1);
        assertThat(goal("stranger@example.com", "2026-11").getTargetMetres()).isEqualTo(50_000);
    }

    @Test
    void rejectsUnauthenticatedRequests() throws Exception {
        mockMvc.perform(put("/api/goals/2026-11")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetDistanceKm\":50}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Not authenticated."));

        mockMvc.perform(get("/api/goals/2026-11"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Not authenticated."));

        mockMvc.perform(delete("/api/goals/2026-11"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Not authenticated."));
    }
}
