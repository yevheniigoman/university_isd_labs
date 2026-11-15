package com.runningtracker.chart;

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
import java.time.YearMonth;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * REQ-F-13: the chart data always has twelve Months ending with the current
 * Month, oldest first, with a zero for every Month that has no Trainings.
 */
@SpringBootTest
@AutoConfigureMockMvc
class MonthComparisonTest {

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

    private void saveTraining(UserAccount owner, LocalDate date, int distanceMetres) {
        trainings.save(new Training(date, distanceMetres, 1800, owner));
    }

    @Test
    void returnsTwelveZeroFilledMonthsWhenThereAreNoTrainings() throws Exception {
        MockHttpSession session = registerAndLogin("runner@example.com");
        YearMonth current = YearMonth.now();

        mockMvc.perform(get("/api/month-comparison").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.months", hasSize(12)))
                .andExpect(jsonPath("$.months[0].month").value(current.minusMonths(11).toString()))
                .andExpect(jsonPath("$.months[11].month").value(current.toString()))
                .andExpect(jsonPath("$.months[*].totalDistanceKm", everyItem(is(0.0))));
    }

    @Test
    void fillsAGapMonthWithZero() throws Exception {
        MockHttpSession session = registerAndLogin("runner@example.com");
        UserAccount owner = account("runner@example.com");
        YearMonth current = YearMonth.now();
        YearMonth twoMonthsAgo = current.minusMonths(2);
        YearMonth oneMonthAgo = current.minusMonths(1);

        saveTraining(owner, twoMonthsAgo.atDay(15), 12300);
        saveTraining(owner, current.atDay(1), 8000);

        mockMvc.perform(get("/api/month-comparison").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.months", hasSize(12)))
                .andExpect(jsonPath("$.months[9].month").value(twoMonthsAgo.toString()))
                .andExpect(jsonPath("$.months[9].totalDistanceKm").value(12.3))
                .andExpect(jsonPath("$.months[10].month").value(oneMonthAgo.toString()))
                .andExpect(jsonPath("$.months[10].totalDistanceKm").value(0.0))
                .andExpect(jsonPath("$.months[11].totalDistanceKm").value(8.0));
    }

    @Test
    void ignoresTrainingsOutsideTheTwelveMonthRange() throws Exception {
        MockHttpSession session = registerAndLogin("runner@example.com");
        UserAccount owner = account("runner@example.com");
        YearMonth current = YearMonth.now();
        YearMonth oldestIncluded = current.minusMonths(11);

        saveTraining(owner, current.minusMonths(12).atDay(1), 9900);
        saveTraining(owner, oldestIncluded.atDay(1), 6000);

        mockMvc.perform(get("/api/month-comparison").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.months", hasSize(12)))
                .andExpect(jsonPath("$.months[0].month").value(oldestIncluded.toString()))
                .andExpect(jsonPath("$.months[0].totalDistanceKm").value(6.0));
    }

    @Test
    void usesOnlyTheAuthenticatedUsersTrainings() throws Exception {
        YearMonth current = YearMonth.now();

        registerAndLogin("stranger@example.com");
        saveTraining(account("stranger@example.com"), current.atDay(1), 9900);

        MockHttpSession session = registerAndLogin("runner@example.com");
        saveTraining(account("runner@example.com"), current.atDay(2), 5200);

        mockMvc.perform(get("/api/month-comparison").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.months[11].totalDistanceKm").value(5.2));
    }

    @Test
    void rejectsUnauthenticatedRequests() throws Exception {
        mockMvc.perform(get("/api/month-comparison"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Not authenticated."));
    }
}
