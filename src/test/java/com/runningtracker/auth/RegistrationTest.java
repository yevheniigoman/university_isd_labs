package com.runningtracker.auth;

import com.runningtracker.repository.MonthlyGoalRepository;
import com.runningtracker.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RegistrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MonthlyGoalRepository goals;

    @Autowired
    private UserAccountRepository users;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanDatabase() {
        goals.deleteAll();
        users.deleteAll();
    }

    private void register(String email, String password) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    void registersNewAccount() throws Exception {
        register("runner@example.com", "correct-horse-battery");

        assertThat(users.count()).isEqualTo(1);
        var account = users.findAll().getFirst();
        assertThat(account.getEmail()).isEqualTo("runner@example.com");
        assertThat(account.getPasswordHash()).isNotEqualTo("correct-horse-battery");
        assertThat(passwordEncoder.matches("correct-horse-battery", account.getPasswordHash())).isTrue();
    }

    @Test
    void storesEmailInLowerCase() throws Exception {
        register("Runner@Example.COM", "correct-horse-battery");

        assertThat(users.findAll().getFirst().getEmail()).isEqualTo("runner@example.com");
    }

    @Test
    void neverDisclosesAnAlreadyRegisteredEmail() throws Exception {
        register("runner@example.com", "correct-horse-battery");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"runner@example.com\",\"password\":\"different-password\"}"))
                .andExpect(status().isNoContent());

        assertThat(users.count()).isEqualTo(1);
    }

    @Test
    void comparesEmailsCaseInsensitivelyWhenRegistering() throws Exception {
        register("runner@example.com", "correct-horse-battery");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"RUNNER@EXAMPLE.COM\",\"password\":\"different-password\"}"))
                .andExpect(status().isNoContent());

        assertThat(users.count()).isEqualTo(1);
    }

    @Test
    void rejectsMalformedEmail() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"not-an-email\",\"password\":\"correct-horse-battery\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid email format."));

        assertThat(users.count()).isZero();
    }

    @Test
    void rejectsEmptyPassword() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"runner@example.com\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest());

        assertThat(users.count()).isZero();
    }
}
