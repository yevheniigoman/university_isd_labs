package com.runningtracker.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the session cookie against a real servlet container. MockMvc does
 * not emulate the container's {@code Set-Cookie} handling, so this contract
 * (REQ-F-3: persistent, HttpOnly, server-side session) is checked over HTTP.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:running-cookie-test;DB_CLOSE_DELAY=-1")
class LoginCookieTest {

    @LocalServerPort
    private int port;

    private final HttpClient http = HttpClient.newHttpClient();

    @Test
    void loginEstablishesAPersistentHttpOnlySessionCookie() throws Exception {
        post("/api/auth/register", "{\"email\":\"cookie@example.com\",\"password\":\"correct-horse\"}");

        HttpResponse<String> login = post("/api/auth/login",
                "{\"email\":\"cookie@example.com\",\"password\":\"correct-horse\"}");

        assertThat(login.statusCode()).isEqualTo(200);

        String setCookie = login.headers().firstValue("Set-Cookie").orElseThrow();
        assertThat(setCookie).contains("SESSION=");
        assertThat(setCookie.toLowerCase()).contains("httponly");
        assertThat(setCookie.toLowerCase()).contains("max-age=");
    }

    private HttpResponse<String> post(String path, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
