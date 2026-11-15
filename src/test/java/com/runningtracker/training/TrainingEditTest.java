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
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * REQ-F-10: an owner replaces a Training's Date, Distance, and Duration under
 * exactly the same validation rules as creation (REQ-F-5, REQ-F-6, REQ-F-7).
 */
@SpringBootTest
@AutoConfigureMockMvc
class TrainingEditTest {

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

    private Training saveTraining(UserAccount owner, LocalDate date, int distanceMetres, int durationSeconds) {
        return trainings.save(new Training(date, distanceMetres, durationSeconds, owner));
    }

    private ResultActions update(MockHttpSession session, long id, String date, String distanceKm, String durationSeconds)
            throws Exception {
        return mockMvc.perform(put("/api/trainings/" + id)
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"date\":\"" + date + "\","
                        + "\"distanceKm\":" + distanceKm + ","
                        + "\"durationSeconds\":" + durationSeconds + "}"));
    }

    private Training reload(long id) {
        return trainings.findById(id).orElseThrow();
    }

    @Test
    void replacesTheFieldsAndNormalisesThem() throws Exception {
        MockHttpSession session = registerAndLogin("runner@example.com");
        Training training = saveTraining(account("runner@example.com"),
                LocalDate.now().minusDays(3), 5200, 1800);
        LocalDate newDate = LocalDate.now().minusDays(1);

        update(session, training.getId(), newDate.toString(), "5.239", "1800.9")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(training.getId().intValue()))
                .andExpect(jsonPath("$.date").value(newDate.toString()))
                .andExpect(jsonPath("$.distanceKm").value(5.23))
                .andExpect(jsonPath("$.durationSeconds").value(1800));

        Training saved = reload(training.getId());
        assertThat(saved.getDate()).isEqualTo(newDate);
        assertThat(saved.getDistanceMetres()).isEqualTo(5230);
        assertThat(saved.getDurationSeconds()).isEqualTo(1800);
    }

    @Test
    void reDatesAcrossMonthsAndRecomputesBothMonthTotals() throws Exception {
        MockHttpSession session = registerAndLogin("runner@example.com");
        UserAccount owner = account("runner@example.com");

        LocalDate thisMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate lastMonth = thisMonth.minusMonths(1);

        Training moved = saveTraining(owner, lastMonth.withDayOfMonth(1), 5200, 1800);
        saveTraining(owner, lastMonth.withDayOfMonth(2), 4800, 1800);

        update(session, moved.getId(), thisMonth.toString(), "6.0", "2000")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value(thisMonth.toString()));

        mockMvc.perform(get("/api/trainings").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.months[0].month").value(YearMonth.from(thisMonth).toString()))
                .andExpect(jsonPath("$.months[0].totalDistanceKm").value(6.0))
                .andExpect(jsonPath("$.months[1].month").value(YearMonth.from(lastMonth).toString()))
                .andExpect(jsonPath("$.months[1].totalDistanceKm").value(4.8));
    }

    @Test
    void rejectsADateLaterThanTomorrowAndLeavesTheTrainingUnchanged() throws Exception {
        MockHttpSession session = registerAndLogin("runner@example.com");
        LocalDate seedDate = LocalDate.now().minusDays(3);
        Training training = saveTraining(account("runner@example.com"),
                seedDate, 5200, 1800);

        update(session, training.getId(), LocalDate.now().plusDays(2).toString(), "6.0", "2000")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Date must not be later than tomorrow."));

        Training saved = reload(training.getId());
        assertThat(saved.getDate()).isEqualTo(seedDate);
        assertThat(saved.getDistanceMetres()).isEqualTo(5200);
        assertThat(saved.getDurationSeconds()).isEqualTo(1800);
    }

    @Test
    void rejectsAnOutOfRangeDistanceAndLeavesTheTrainingUnchanged() throws Exception {
        MockHttpSession session = registerAndLogin("runner@example.com");
        Training training = saveTraining(account("runner@example.com"),
                LocalDate.now().minusDays(3), 5200, 1800);

        update(session, training.getId(), LocalDate.now().minusDays(1).toString(), "100.01", "2000")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Distance must be between 0.01 and 100 km."));

        assertThat(reload(training.getId()).getDistanceMetres()).isEqualTo(5200);
    }

    @Test
    void rejectsAZeroDurationAndLeavesTheTrainingUnchanged() throws Exception {
        MockHttpSession session = registerAndLogin("runner@example.com");
        Training training = saveTraining(account("runner@example.com"),
                LocalDate.now().minusDays(3), 5200, 1800);

        update(session, training.getId(), LocalDate.now().minusDays(1).toString(), "6.0", "0")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Duration must be greater than zero."));

        assertThat(reload(training.getId()).getDurationSeconds()).isEqualTo(1800);
    }

    @Test
    void returnsNotFoundForAnotherUsersTrainingAndLeavesItUnchanged() throws Exception {
        registerAndLogin("stranger@example.com");
        Training strangersTraining = saveTraining(account("stranger@example.com"),
                LocalDate.now().minusDays(3), 5200, 1800);
        MockHttpSession session = registerAndLogin("runner@example.com");

        update(session, strangersTraining.getId(), LocalDate.now().minusDays(1).toString(), "6.0", "2000")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Training not found."));

        assertThat(reload(strangersTraining.getId()).getDistanceMetres()).isEqualTo(5200);
    }

    @Test
    void returnsNotFoundForAnUnknownIdentifier() throws Exception {
        MockHttpSession session = registerAndLogin("runner@example.com");

        update(session, 999999, LocalDate.now().minusDays(1).toString(), "6.0", "2000")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Training not found."));
    }

    @Test
    void rejectsUnauthenticatedRequests() throws Exception {
        Training training = saveTraining(
                users.save(new UserAccount("runner@example.com", "hash")),
                LocalDate.now().minusDays(3), 5200, 1800);

        mockMvc.perform(put("/api/trainings/" + training.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"date\":\"" + LocalDate.now().minusDays(1)
                                + "\",\"distanceKm\":6.0,\"durationSeconds\":2000}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Not authenticated."));

        assertThat(reload(training.getId()).getDistanceMetres()).isEqualTo(5200);
    }
}
