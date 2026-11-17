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

import java.time.YearMonth;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * REQ-F-15: the goal listing covers the twelve Months of the Month Comparison
 * Range, oldest first, omitting Months without a goal.
 */
@SpringBootTest
@AutoConfigureMockMvc
class GoalListTest {

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

    private void setGoal(MockHttpSession session, YearMonth month, String targetKm) throws Exception {
        mockMvc.perform(put("/api/goals/" + month)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetDistanceKm\":" + targetKm + "}"))
                .andExpect(status().isOk());
    }

    @Test
    void returnsAnEmptyListWhenThereAreNoGoals() throws Exception {
        MockHttpSession session = registerAndLogin("runner@example.com");

        mockMvc.perform(get("/api/goals").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goals", hasSize(0)));
    }

    @Test
    void listsOnlyGoalsInTheTwelveMonthRangeOldestFirst() throws Exception {
        MockHttpSession session = registerAndLogin("runner@example.com");
        YearMonth current = YearMonth.now();
        YearMonth oldestIncluded = current.minusMonths(11);
        YearMonth twoMonthsAgo = current.minusMonths(2);

        setGoal(session, current.minusMonths(12), "10");
        setGoal(session, current.plusMonths(1), "10");
        setGoal(session, current, "40");
        setGoal(session, oldestIncluded, "20");
        setGoal(session, twoMonthsAgo, "30");

        mockMvc.perform(get("/api/goals").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goals", hasSize(3)))
                .andExpect(jsonPath("$.goals[0].month").value(oldestIncluded.toString()))
                .andExpect(jsonPath("$.goals[0].targetDistanceKm").value(20.0))
                .andExpect(jsonPath("$.goals[1].month").value(twoMonthsAgo.toString()))
                .andExpect(jsonPath("$.goals[1].targetDistanceKm").value(30.0))
                .andExpect(jsonPath("$.goals[2].month").value(current.toString()))
                .andExpect(jsonPath("$.goals[2].targetDistanceKm").value(40.0));
    }

    @Test
    void omitsMonthsWithoutAGoal() throws Exception {
        MockHttpSession session = registerAndLogin("runner@example.com");
        YearMonth current = YearMonth.now();

        setGoal(session, current.minusMonths(2), "20");
        setGoal(session, current, "40");

        mockMvc.perform(get("/api/goals").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goals", hasSize(2)))
                .andExpect(jsonPath("$.goals[0].month").value(current.minusMonths(2).toString()))
                .andExpect(jsonPath("$.goals[1].month").value(current.toString()));
    }

    @Test
    void includesProgressForEachGoal() throws Exception {
        MockHttpSession session = registerAndLogin("runner@example.com");
        UserAccount owner = account("runner@example.com");
        YearMonth current = YearMonth.now();

        trainings.save(new Training(current.atDay(1), 5_200, 1800, owner));
        setGoal(session, current, "50");

        mockMvc.perform(get("/api/goals").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goals", hasSize(1)))
                .andExpect(jsonPath("$.goals[0].month").value(current.toString()))
                .andExpect(jsonPath("$.goals[0].targetDistanceKm").value(50.0))
                .andExpect(jsonPath("$.goals[0].completedDistanceKm").value(5.2))
                .andExpect(jsonPath("$.goals[0].remainingDistanceKm").value(44.8))
                .andExpect(jsonPath("$.goals[0].percentComplete").value(10.4));
    }

    @Test
    void usesOnlyTheAuthenticatedUsersGoals() throws Exception {
        YearMonth current = YearMonth.now();

        MockHttpSession strangersSession = registerAndLogin("stranger@example.com");
        setGoal(strangersSession, current, "99");

        MockHttpSession session = registerAndLogin("runner@example.com");
        setGoal(session, current, "50");

        mockMvc.perform(get("/api/goals").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goals", hasSize(1)))
                .andExpect(jsonPath("$.goals[0].targetDistanceKm").value(50.0));
    }

    @Test
    void rejectsUnauthenticatedRequests() throws Exception {
        mockMvc.perform(get("/api/goals"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Not authenticated."));
    }
}
