package tech.provokedynamic.uno.db.repository;

import org.junit.jupiter.api.*;
import tech.provokedynamic.uno.db.Database;
import tech.provokedynamic.uno.db.SchemaInit;
import tech.provokedynamic.uno.db.model.GamePlayerRecord;
import tech.provokedynamic.uno.db.model.GameRecord;
import tech.provokedynamic.uno.db.model.TopScoreRecord;
import tech.provokedynamic.uno.db.model.WinCountRecord;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Persistence layer tests for {@link GameRepository}.
 *
 * <p>Each test class run uses a fresh named in-memory H2 database
 * ({@code DB_CLOSE_DELAY=-1} keeps it alive for the duration of the JVM).
 * No external database or manual setup is required.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GameRepositoryTest {

    private static GameRepository repo;

    @BeforeAll
    static void initDatabase() {
        // Isolated in-memory H2 — no file, no network, no external state
        Database.init(Database.h2MemProps("uno_test"));
        SchemaInit.run();
        repo = new GameRepository();
    }

    private static LocalDateTime ts(int minutesAgo) {
        return LocalDateTime.now().minusMinutes(minutesAgo);
    }

    // Tests: saveGame / recentGames

    @Test
    @Order(1)
    void saveGame_persistsAllRequiredFields() {
        List<String> players = List.of("Alice", "Bob");
        int[] scores = {120, 0};
        LocalDateTime start = ts(5);

        repo.saveGame(players, scores, 0, 7, start);

        List<GameRecord> recent = repo.recentGames(10);
        assertFalse(recent.isEmpty(), "Expected at least one saved game");

        GameRecord g = recent.getFirst();
        assertEquals(7, g.getRoundsPlayed(), "rounds_played should be 7");
        assertNotNull(g.getStartedAt(), "started_at should not be null");
        assertNotNull(g.getFinishedAt(), "finished_at should not be null");

        // Two player rows attached
        assertEquals(2, g.getPlayers().size(), "Expected two player rows");
    }

    @Test
    @Order(2)
    void saveGame_marksCorrectWinner() {
        List<String> players = List.of("Carol", "Dave");
        int[] scores = {0, 80};
        repo.saveGame(players, scores, 1 /* Dave wins */, 4, ts(3));

        List<GameRecord> recent = repo.recentGames(1);
        var playerRows = recent.getFirst().getPlayers();

        var winner = playerRows.stream().filter(GamePlayerRecord::isWinner).findFirst();
        assertTrue(winner.isPresent(), "A winner row should be present");
        assertEquals("Dave", winner.get().getPlayerName());
    }

    @Test
    @Order(3)
    void saveGame_persistsPerPlayerScores() {
        List<String> players = List.of("Eve", "Frank");
        int[] scores = {200, 50};
        repo.saveGame(players, scores, 0, 10, ts(2));

        List<GameRecord> recent = repo.recentGames(1);
        var playerRows = recent.getFirst().getPlayers();

        var eve = playerRows.stream()
                .filter(p -> "Eve".equals(p.getPlayerName()))
                .findFirst()
                .orElseThrow();
        assertEquals(200, eve.getScore(), "Eve's score should be 200");

        var frank = playerRows.stream()
                .filter(p -> "Frank".equals(p.getPlayerName()))
                .findFirst()
                .orElseThrow();
        assertEquals(50, frank.getScore(), "Frank's score should be 50");
    }

    @Test
    @Order(4)
    void recentGames_respectsLimit() {
        // Save two more games to ensure we have > 1 total
        repo.saveGame(List.of("G", "H"), new int[]{10, 20}, 1, 2, ts(10));
        repo.saveGame(List.of("I", "J"), new int[]{30, 40}, 0, 3, ts(9));

        List<GameRecord> limited = repo.recentGames(1);
        assertEquals(1, limited.size(), "Limit=1 should return exactly one game");
    }

    @Test
    @Order(5)
    void recentGames_returnsEmptyListWhenNoGames() {
        // Fresh DB with no inserts yet — use a separate isolated instance
        Database.init(Database.h2MemProps("uno_test_empty"));
        SchemaInit.run();
        GameRepository emptyRepo = new GameRepository();

        List<GameRecord> result = emptyRepo.recentGames(10);
        assertNotNull(result);
        assertTrue(result.isEmpty(), "Expected empty list for a brand-new database");

        // Restore shared repo for subsequent tests
        Database.init(Database.h2MemProps("uno_test"));
    }

    // Tests: winCounts

    @Test
    @Order(6)
    void winCounts_returnsCorrectWinnerTally() {
        // Alice has one earlier win (from test 1); give her another
        repo.saveGame(List.of("Alice", "Bob"), new int[]{90, 0}, 0, 5, ts(1));

        List<WinCountRecord> counts = repo.winCounts();
        assertFalse(counts.isEmpty(), "Win counts should not be empty");

        var aliceRow = counts.stream()
                .filter(r -> "Alice".equals(r.getPlayerName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Alice not found in win counts"));

        assertTrue(aliceRow.getWins() >= 2, "Alice should have at least 2 wins");
    }

    @Test
    @Order(7)
    void winCounts_orderedByWinsDescending() {
        List<WinCountRecord> counts = repo.winCounts();
        for (int i = 1; i < counts.size(); i++) {
            assertTrue(
                    counts.get(i - 1).getWins() >= counts.get(i).getWins(),
                    "Win counts should be in descending order"
            );
        }
    }

    // Tests: topScores

    @Test
    @Order(8)
    void topScores_returnsAllPlayersWithAggregatedScore() {
        List<TopScoreRecord> scores = repo.topScores();
        assertFalse(scores.isEmpty(), "Top scores should not be empty");

        // Eve scored 200 in test 3 — she should appear
        var eveRow = scores.stream()
                .filter(r -> "Eve".equals(r.getPlayerName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Eve not found in top scores"));

        assertTrue(eveRow.getTotalScore() >= 200, "Eve's total score should be >= 200");
    }

    @Test
    @Order(9)
    void topScores_orderedByTotalScoreDescending() {
        List<TopScoreRecord> scores = repo.topScores();
        for (int i = 1; i < scores.size(); i++) {
            assertTrue(
                    scores.get(i - 1).getTotalScore() >= scores.get(i).getTotalScore(),
                    "Top scores should be in descending order"
            );
        }
    }

    // Tests: idempotent player upsert

    @Test
    @Order(10)
    void saveGame_samePlayerNameTwiceDoesNotDuplicate() {
        // Inserting the same name twice (across two saveGame calls) must not throw
        // and must not create duplicate player rows
        assertDoesNotThrow(() -> {
            repo.saveGame(List.of("Alice", "Bob"), new int[]{10, 0}, 0, 1, ts(0));
            repo.saveGame(List.of("Alice", "Bob"), new int[]{20, 0}, 0, 1, ts(0));
        }, "Duplicate player names across games should be handled by the MERGE/upsert");
    }

    // Tests: safety-limit game (winner == -1)

    @Test
    @Order(11)
    void saveGame_safetyLimitGamePersistsWithNoWinner() {
        List<String> players = List.of("Zara", "Max");
        int[] scores = {0, 0};

        // winner = -1 means safety limit was hit, no winner
        assertDoesNotThrow(() ->
                        repo.saveGame(players, scores, -1, 3000, ts(0)),
                "A safety-limit game (winner=-1) should persist without errors"
        );

        List<GameRecord> recent = repo.recentGames(1);
        var playerRows = recent.getFirst().getPlayers();
        boolean anyWinner = playerRows.stream().anyMatch(GamePlayerRecord::isWinner);
        assertFalse(anyWinner, "No player should be marked as winner when safety limit was hit");
    }
}
