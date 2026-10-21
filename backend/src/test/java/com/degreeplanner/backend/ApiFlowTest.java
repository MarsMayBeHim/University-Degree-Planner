package com.degreeplanner.backend;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = "server.port=18080")
class ApiFlowTest {

    private static final String BASE = "http://localhost:18080";

    private final HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    private HttpResponse<String> send(String method, String path, String token, String json)
            throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(BASE + path))
                .header("Content-Type", "application/json");
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        builder.method(method, json == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(json));
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static String firstMatch(String body, String regex) {
        Matcher m = Pattern.compile(regex).matcher(body);
        assertThat(m.find()).as("pattern " + regex + " in " + body).isTrue();
        return m.group(1);
    }

    private String newToken() throws Exception {
        String email = "test-" + UUID.randomUUID() + "@example.com";
        var res = send("POST", "/api/auth/register", null,
                "{\"email\":\"" + email + "\",\"password\":\"password123\"}");
        assertThat(res.statusCode()).isEqualTo(200);
        return firstMatch(res.body(), "\"token\":\"([^\"]+)\"");
    }

    private String cs110Id() throws Exception {
        var res = send("GET", "/api/courses?q=CS%20110", null, null);
        return firstMatch(res.body(), "\"id\":(\\d+),\"code\":\"CS 110\"");
    }

    private void chooseProgram(String token) throws Exception {
        String programId = firstMatch(send("GET", "/api/programs", null, null).body(), "\"id\":(\\d+)");
        var res = send("PUT", "/api/me/profile", token,
                "{\"programId\":" + programId + ",\"startYear\":2025,\"startTerm\":\"FALL\"}");
        assertThat(res.statusCode()).isEqualTo(200);
    }

    @Test
    void publicEndpointsWorkWithoutLogin() throws Exception {
        assertThat(send("GET", "/api/health", null, null).statusCode()).isEqualTo(200);

        var courses = send("GET", "/api/courses?q=CS%20110", null, null);
        assertThat(courses.statusCode()).isEqualTo(200);
        assertThat(courses.body()).contains("CS 110");

        assertThat(send("GET", "/api/programs", null, null).body()).contains("Computer Science");
    }

    @Test
    void protectedEndpointsRejectMissingOrBadTokens() throws Exception {
        for (String path : new String[] {"/api/me", "/api/me/courses", "/api/me/progress", "/api/me/plan"}) {
            assertThat(send("GET", path, null, null).statusCode()).as(path).isEqualTo(401);
            assertThat(send("GET", path, "not-a-real-token", null).statusCode()).as(path).isEqualTo(401);
        }
    }

    @Test
    void registerAndLoginRules() throws Exception {
        String email = "rules-" + UUID.randomUUID() + "@example.com";
        String body = "{\"email\":\"" + email + "\",\"password\":\"password123\"}";

        assertThat(send("POST", "/api/auth/register", null, body).statusCode()).isEqualTo(200);
        assertThat(send("POST", "/api/auth/register", null, body).statusCode()).isEqualTo(409);
        assertThat(send("POST", "/api/auth/login", null, body).statusCode()).isEqualTo(200);

        String wrong = "{\"email\":\"" + email + "\",\"password\":\"wrongpassword\"}";
        assertThat(send("POST", "/api/auth/login", null, wrong).statusCode()).isEqualTo(401);

        String tooShort = "{\"email\":\"short-" + UUID.randomUUID() + "@example.com\",\"password\":\"abc\"}";
        assertThat(send("POST", "/api/auth/register", null, tooShort).statusCode()).isEqualTo(400);
    }

    @Test
    void eachUserOnlySeesTheirOwnCourses() throws Exception {
        String alice = newToken();
        String bob = newToken();

        var saved = send("POST", "/api/me/courses", alice,
                "{\"courseId\":" + cs110Id() + ",\"status\":\"COMPLETED\"}");
        assertThat(saved.statusCode()).isEqualTo(200);

        assertThat(send("GET", "/api/me/courses", alice, null).body()).contains("CS 110");
        assertThat(send("GET", "/api/me/courses", bob, null).body()).isEqualTo("[]");
    }

    @Test
    void progressNeedsAProgramAndReflectsCompletedCourses() throws Exception {
        String token = newToken();
        assertThat(send("GET", "/api/me/progress", token, null).statusCode()).isEqualTo(400);

        chooseProgram(token);
        assertThat(send("GET", "/api/me/progress", token, null).statusCode()).isEqualTo(200);

        send("POST", "/api/me/courses", token,
                "{\"courseId\":" + cs110Id() + ",\"status\":\"COMPLETED\"}");
        var progress = send("GET", "/api/me/progress", token, null);
        assertThat(progress.body()).contains("\"completedCourses\":[\"CS 110\"]");
    }

    @Test
    void generatedPlanIsSavedAndReadable() throws Exception {
        String token = newToken();
        chooseProgram(token);

        var generated = send("POST", "/api/me/plan/generate", token, "{\"maxCredits\":12}");
        assertThat(generated.statusCode()).isEqualTo(200);
        assertThat(generated.body()).contains("\"coursesPlaced\":");

        var plan = send("GET", "/api/me/plan", token, null);
        assertThat(plan.statusCode()).isEqualTo(200);
        assertThat(plan.body()).contains("Fall 2025").contains("CS 110");
    }
}