package sg.sports.bowling;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end test of every REST endpoint against a real (H2) database,
 * covering the full flow: register/login -> manage bowlers/sessions/games
 * (as admin) -> submit frame scores -> read back scores and leaderboard.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BowlingApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ── Auth ─────────────────────────────────────────────────────────────

    @Test
    void registerThenLoginThenMe() throws Exception {
        String registerBody = """
            { "username": "alice", "email": "alice@example.com", "password": "password123" }
        """;
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully"));

        String token = loginAndGetToken("alice", "password123");
        assertThat(token).isNotBlank();

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));
    }

    @Test
    void loginWithBadCredentialsIsRejected() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            { "username": "admin", "password": "wrong-password" }
                        """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshIssuesANewToken() throws Exception {
        String token = loginAsAdmin();

        MvcResult result = mockMvc.perform(post("/api/auth/refresh")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        String newToken = readField(result, "token");
        assertThat(newToken).isNotBlank();
    }

    @Test
    void protectedEndpointWithoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/bowlers"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changePasswordWithCorrectCurrentPasswordAllowsLoginWithNewPassword() throws Exception {
        registerUser("erin", "erin@example.com", "password123");
        String token = loginAndGetToken("erin", "password123");

        mockMvc.perform(post("/api/auth/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"currentPassword\": \"password123\", \"newPassword\": \"newpassword456\" }"))
                .andExpect(status().isOk());

        String newToken = loginAndGetToken("erin", "newpassword456");
        assertThat(newToken).isNotBlank();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"username\": \"erin\", \"password\": \"password123\" }"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changePasswordWithWrongCurrentPasswordIsRejected() throws Exception {
        registerUser("frank", "frank@example.com", "password123");
        String token = loginAndGetToken("frank", "password123");

        mockMvc.perform(post("/api/auth/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"currentPassword\": \"wrong-password\", \"newPassword\": \"newpassword456\" }"))
                .andExpect(status().isBadRequest());
    }

    // ── Bowlers ──────────────────────────────────────────────────────────

    @Test
    void adminCanCreateUpdateAndDeleteABowler() throws Exception {
        String adminToken = loginAsAdmin();

        MvcResult createResult = mockMvc.perform(post("/api/bowlers")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"name\": \"Carol\" }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Carol"))
                .andReturn();
        long bowlerId = readField(createResult, "id");

        mockMvc.perform(get("/api/bowlers/" + bowlerId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Carol"));

        mockMvc.perform(put("/api/bowlers/" + bowlerId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"name\": \"Caroline\" }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Caroline"));

        mockMvc.perform(delete("/api/bowlers/" + bowlerId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void deletingABowlerWithExistingGameHistoryIsRejected() throws Exception {
        String adminToken = loginAsAdmin();
        long bowlerId = createBowler(adminToken, "Deletable Dan");

        MvcResult sessionResult = mockMvc.perform(post("/api/sessions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"sessionDate\": \"2026-06-27\", \"timeSlot\": \"MORNING\" }"))
                .andExpect(status().isOk())
                .andReturn();
        long sessionId = readField(sessionResult, "id");

        MvcResult gameResult = mockMvc.perform(post("/api/sessions/" + sessionId + "/games")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();
        long gameId = readField(gameResult, "id");

        submitFrames(adminToken, bowlerId, gameId, "[[10]]");

        mockMvc.perform(delete("/api/bowlers/" + bowlerId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot delete Deletable Dan: they have existing game history"));

        // Bowler must still exist — the delete should have been rejected, not partially applied.
        mockMvc.perform(get("/api/bowlers/" + bowlerId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void creatingADuplicateBowlerReturnsBadRequestNotUnauthorized() throws Exception {
        String adminToken = loginAsAdmin();
        createBowler(adminToken, "Gina");

        mockMvc.perform(post("/api/bowlers")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"name\": \"Gina\" }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Bowler already exists: Gina"));
    }

    @Test
    void nonAdminCannotCreateABowler() throws Exception {
        registerUser("dave", "dave@example.com", "password123");
        String token = loginAndGetToken("dave", "password123");

        mockMvc.perform(post("/api/bowlers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"name\": \"Dave\" }"))
                .andExpect(status().isForbidden());
    }

    // ── Sessions, games, scores, leaderboard (full flow) ────────────────

    @Test
    void adminCanDeleteSessionWithAllAssociatedData() throws Exception {
        String adminToken = loginAsAdmin();
        long bowlerId = createBowler(adminToken, "Henry");

        MvcResult sessionResult = mockMvc.perform(post("/api/sessions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"sessionDate\": \"2026-06-28\", \"timeSlot\": \"EVENING\" }"))
                .andExpect(status().isOk())
                .andReturn();
        long sessionId = readField(sessionResult, "id");

        MvcResult gameResult = mockMvc.perform(post("/api/sessions/" + sessionId + "/games")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();
        long gameId = readField(gameResult, "id");

        submitFrames(adminToken, bowlerId, gameId, "[[10]]");

        mockMvc.perform(delete("/api/sessions/" + sessionId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/sessions/" + sessionId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void nonAdminCannotDeleteSession() throws Exception {
        String adminToken = loginAsAdmin();
        MvcResult sessionResult = mockMvc.perform(post("/api/sessions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"sessionDate\": \"2026-06-29\", \"timeSlot\": \"MORNING\" }"))
                .andExpect(status().isOk())
                .andReturn();
        long sessionId = readField(sessionResult, "id");

        registerUser("ivan", "ivan@example.com", "password123");
        String userToken = loginAndGetToken("ivan", "password123");

        mockMvc.perform(delete("/api/sessions/" + sessionId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void fullSessionScoringFlowProducesExpectedLeaderboard() throws Exception {
        String adminToken = loginAsAdmin();

        long aliceId = createBowler(adminToken, "Alice");
        long bobId = createBowler(adminToken, "Bob");

        MvcResult sessionResult = mockMvc.perform(post("/api/sessions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"sessionDate\": \"2026-06-27\", \"location\": \"Downtown Lanes\", \"timeSlot\": \"MORNING\" }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.timeSlot").value("MORNING"))
                .andReturn();
        long sessionId = readField(sessionResult, "id");

        mockMvc.perform(get("/api/sessions/open")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + sessionId + ")]").exists());

        MvcResult gameResult = mockMvc.perform(post("/api/sessions/" + sessionId + "/games")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameNumber").value(1))
                .andReturn();
        long gameId = readField(gameResult, "id");

        // Alice bowls a perfect game (12 strikes -> 300, 12 frame points from strikes... only 10 frames count)
        submitFrames(adminToken, aliceId, gameId, """
            [[10],[10],[10],[10],[10],[10],[10],[10],[10],[10,10,10]]
        """);

        // Bob bowls all open frames (3,4) -> total 70, no spares/strikes
        submitFrames(adminToken, bobId, gameId, """
            [[3,4],[3,4],[3,4],[3,4],[3,4],[3,4],[3,4],[3,4],[3,4],[3,4]]
        """);

        // Alice's frames: 10 strikes -> each frame_points = 2 -> 20 frame points, plus she wins -> +3
        mockMvc.perform(get("/api/scores/frames")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("bowlerId", String.valueOf(aliceId))
                        .param("gameId", String.valueOf(gameId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].strike").value(true))
                .andExpect(jsonPath("$[0].framePoints").value(2.0))
                .andExpect(jsonPath("$[9].cumulativeScore").value(300));

        mockMvc.perform(get("/api/scores/games/" + gameId + "/participants")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.bowlerId == " + aliceId + ")].totalScore").value(300))
                .andExpect(jsonPath("$[?(@.bowlerId == " + bobId + ")].totalScore").value(70));

        MvcResult leaderboardResult = mockMvc.perform(get("/api/scores/leaderboard")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("sessionId", String.valueOf(sessionId)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode leaderboard = objectMapper.readTree(leaderboardResult.getResponse().getContentAsString());
        double alicePoints = pointsFor(leaderboard, aliceId);
        double bobPoints = pointsFor(leaderboard, bobId);

        assertThat(alicePoints).isEqualTo(23.0); // 20 frame points (10 strikes) + 3 win bonus
        assertThat(bobPoints).isEqualTo(0.0);

        mockMvc.perform(post("/api/sessions/" + sessionId + "/close")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }

    @Test
    void creatingDuplicateSessionIsRejected() throws Exception {
        String adminToken = loginAsAdmin();

        mockMvc.perform(post("/api/sessions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"sessionDate\": \"2026-07-01\", \"timeSlot\": \"AFTERNOON\" }"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/sessions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"sessionDate\": \"2026-07-01\", \"timeSlot\": \"AFTERNOON\" }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A session already exists for 2026-07-01 (afternoon)"));
    }

    @Test
    void creatingSessionWithoutTimeSlotIsRejected() throws Exception {
        String adminToken = loginAsAdmin();

        mockMvc.perform(post("/api/sessions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"sessionDate\": \"2026-06-27\" }"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/sessions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"sessionDate\": \"2026-06-27\", \"timeSlot\": \"NIGHT_OWL\" }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resubmittingABowlersGrowingFrameListDoesNotConflictWithPreviousFrames() throws Exception {
        String adminToken = loginAsAdmin();
        long aliceId = createBowler(adminToken, "Hana");

        MvcResult sessionResult = mockMvc.perform(post("/api/sessions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"sessionDate\": \"2026-06-27\", \"timeSlot\": \"EVENING\" }"))
                .andExpect(status().isOk())
                .andReturn();
        long sessionId = readField(sessionResult, "id");

        MvcResult gameResult = mockMvc.perform(post("/api/sessions/" + sessionId + "/games")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();
        long gameId = readField(gameResult, "id");

        // Frame-by-frame entry: submit frame 1, then resubmit frames 1+2, as the
        // "Add game score" page does each time a bowler completes another frame.
        submitFrames(adminToken, aliceId, gameId, "[[10]]");
        submitFrames(adminToken, aliceId, gameId, "[[10],[7,2]]");

        mockMvc.perform(get("/api/scores/frames")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("bowlerId", String.valueOf(aliceId))
                        .param("gameId", String.valueOf(gameId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].cumulativeScore").value(28));
    }

    @Test
    void winLossIsNotDecidedUntilEveryBowlerFinishesAllTenFrames() throws Exception {
        String adminToken = loginAsAdmin();
        long aliceId = createBowler(adminToken, "Ivy");
        long bobId = createBowler(adminToken, "Jack");

        MvcResult sessionResult = mockMvc.perform(post("/api/sessions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"sessionDate\": \"2026-06-27\", \"timeSlot\": \"MORNING\" }"))
                .andExpect(status().isOk())
                .andReturn();
        long sessionId = readField(sessionResult, "id");

        MvcResult gameResult = mockMvc.perform(post("/api/sessions/" + sessionId + "/games")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();
        long gameId = readField(gameResult, "id");

        // Both bowlers tied on frame 1 (a strike each) — only one frame of ten entered so far.
        submitFrames(adminToken, aliceId, gameId, "[[10]]");
        submitFrames(adminToken, bobId, gameId, "[[10]]");

        // Mid-game: no result yet, gamePoints is just the frame points earned so far (not a
        // tied-for-the-lead win-bonus share like 3.5 — the game isn't finished).
        mockMvc.perform(get("/api/scores/games/" + gameId + "/participants")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.bowlerId == " + aliceId + ")].gamePoints").value(2.0))
                .andExpect(jsonPath("$[?(@.bowlerId == " + aliceId + ")].result").value(org.hamcrest.Matchers.contains(org.hamcrest.Matchers.nullValue())))
                .andExpect(jsonPath("$[?(@.bowlerId == " + bobId + ")].gamePoints").value(2.0))
                .andExpect(jsonPath("$[?(@.bowlerId == " + bobId + ")].result").value(org.hamcrest.Matchers.contains(org.hamcrest.Matchers.nullValue())));

        // Alice finishes with a perfect game; Bob still hasn't finished.
        submitFrames(adminToken, aliceId, gameId, """
            [[10],[10],[10],[10],[10],[10],[10],[10],[10],[10,10,10]]
        """);

        mockMvc.perform(get("/api/scores/games/" + gameId + "/participants")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.bowlerId == " + aliceId + ")].result").value(org.hamcrest.Matchers.contains(org.hamcrest.Matchers.nullValue())));

        // Now Bob finishes too, all open frames -> both games complete -> result is finally decided.
        submitFrames(adminToken, bobId, gameId, """
            [[3,4],[3,4],[3,4],[3,4],[3,4],[3,4],[3,4],[3,4],[3,4],[3,4]]
        """);

        mockMvc.perform(get("/api/scores/games/" + gameId + "/participants")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.bowlerId == " + aliceId + ")].result").value("WIN"))
                .andExpect(jsonPath("$[?(@.bowlerId == " + aliceId + ")].gamePoints").value(23.0))
                .andExpect(jsonPath("$[?(@.bowlerId == " + bobId + ")].result").value("LOSS"))
                .andExpect(jsonPath("$[?(@.bowlerId == " + bobId + ")].gamePoints").value(0.0));
    }

    // ── helpers ──────────────────────────────────────────────────────────

    private long createBowler(String adminToken, String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/bowlers")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of("name", name))))
                .andExpect(status().isOk())
                .andReturn();
        return readField(result, "id");
    }

    private void submitFrames(String token, long bowlerId, long gameId, String framesJson) throws Exception {
        String body = """
            { "bowlerId": %d, "gameId": %d, "frames": %s }
        """.formatted(bowlerId, gameId, framesJson);
        mockMvc.perform(post("/api/scores/frames")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    private double pointsFor(JsonNode leaderboard, long bowlerId) {
        for (JsonNode entry : leaderboard) {
            if (entry.get("bowlerId").asLong() == bowlerId) {
                return entry.get("totalPoints").asDouble();
            }
        }
        throw new AssertionError("Bowler " + bowlerId + " not found in leaderboard: " + leaderboard);
    }

    private void registerUser(String username, String email, String password) throws Exception {
        String body = """
            { "username": "%s", "email": "%s", "password": "%s" }
        """.formatted(username, email, password);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    private String loginAsAdmin() throws Exception {
        return loginAndGetToken("admin", "changeme");
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        String body = """
            { "username": "%s", "password": "%s" }
        """.formatted(username, password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        return readField(result, "token");
    }

    @SuppressWarnings("unchecked")
    private <T> T readField(MvcResult result, String field) throws Exception {
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString()).get(field);
        if (node.isLong() || node.isInt()) return (T) Long.valueOf(node.asLong());
        return (T) node.asText();
    }
}
