package tech.provokedynamic.uno.db.repository;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.provokedynamic.uno.db.Database;
import tech.provokedynamic.uno.db.mapper.PlayerMapper;
import tech.provokedynamic.uno.db.model.GamePlayerRecord;
import tech.provokedynamic.uno.db.model.GameRecord;
import tech.provokedynamic.uno.db.model.TopScoreRecord;
import tech.provokedynamic.uno.db.model.WinCountRecord;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Persistence tests for GameRepository.
 * <p>
 * Each test method gets its own isolated in-memory H2 database via
 * a @BeforeEach setup, so tests are fully independent of each other —
 * no @Order annotations, no shared mutable state, no global Database factory.
 */
class GameRepositoryTest {

    private static final AtomicInteger DB_COUNTER = new AtomicInteger();

    private GameRepository repo;

    @BeforeEach
    void initDatabase() throws IOException, SQLException {
        // Each test gets a uniquely named in-memory DB — no cross-test pollution,
        // no reliance on execution order.
        String dbName = "uno_test_" + DB_COUNTER.incrementAndGet();
        Database.init(Database.h2MemProps(dbName));

        try (SqlSession session = Database.factory().openSession();
             Connection conn = session.getConnection()) {
            ScriptRunner runner = new ScriptRunner(conn);
            runner.setLogWriter(null);
            runner.setErrorLogWriter(new PrintWriter(System.err));
            runner.runScript(new InputStreamReader(
                    Resources.getResourceAsStream("db/schema.sql")));
        }

        repo = new GameRepository();
    }

    // saveGame / recentGames

    @Test
    void saveGame_persistsAllRequiredFields() {
        LocalDateTime start = LocalDateTime.now().minusSeconds(30);

        repo.saveGame(List.of("Alice", "Bob"), new int[]{100, 0}, 0, 15, start);

        List<GameRecord> recent = repo.recentGames(5);
        assertFalse(recent.isEmpty(), "Expected at least one saved game");

        GameRecord g = recent.getFirst();
        assertEquals(15, g.getRoundsPlayed());
        assertNotNull(g.getFinishedAt(), "finishedAt must be populated");
        assertEquals(2, g.getPlayers().size(), "Expected one row per player");
    }

    @Test
    void saveGame_marksCorrectPlayerAsWinner() {
        repo.saveGame(List.of("Alice", "Bob"), new int[]{80, 20}, 0, 10, LocalDateTime.now());

        GameRecord g = repo.recentGames(1).getFirst();
        var winner = g.getPlayers().stream().filter(GamePlayerRecord::isWinner).findFirst();

        assertTrue(winner.isPresent(), "Expected exactly one winner");
        assertEquals("Alice", winner.get().getPlayerName());
    }

    @Test
    void saveGame_persistsPerPlayerScores() {
        repo.saveGame(List.of("Alice", "Bob"), new int[]{150, 75}, 0, 20, LocalDateTime.now());

        GameRecord g = repo.recentGames(1).getFirst();
        var alice = g.getPlayers().stream()
                .filter(p -> "Alice".equals(p.getPlayerName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Alice not found"));

        assertEquals(150, alice.getScore());
    }

    @Test
    void saveGame_noWinnerWhenSafetyLimitHit() {
        // winnerIndex = -1 signals that the safety round cap was reached
        repo.saveGame(List.of("Alice", "Bob"), new int[]{0, 0}, -1, 3000, LocalDateTime.now());

        GameRecord g = repo.recentGames(1).getFirst();
        assertEquals(3000, g.getRoundsPlayed());
        assertFalse(g.getPlayers().stream().anyMatch(GamePlayerRecord::isWinner),
                "No player should be marked winner when the safety limit fires");
    }

    @Test
    void recentGames_honorsLimit() {
        for (int i = 0; i < 5; i++) {
            repo.saveGame(List.of("Alice", "Bob"), new int[]{50, 0}, 0, 5, LocalDateTime.now());
        }

        List<GameRecord> recent = repo.recentGames(2);
        assertTrue(recent.size() <= 2, "recentGames(2) must return at most 2 games");
    }

    @Test
    void recentGames_emptyOnFreshDatabase() {
        // No games saved — fresh DB from @BeforeEach
        assertTrue(repo.recentGames(10).isEmpty(),
                "A fresh database should return no games");
    }

    @Test
    void recentGames_returnsNewestFirst() {
        LocalDateTime earlier = LocalDateTime.now().minusMinutes(5);
        LocalDateTime later = LocalDateTime.now();

        repo.saveGame(List.of("Alice", "Bob"), new int[]{10, 0}, 0, 3, earlier);
        repo.saveGame(List.of("Alice", "Bob"), new int[]{99, 0}, 0, 7, later);

        List<GameRecord> recent = repo.recentGames(2);
        assertEquals(7, recent.getFirst().getRoundsPlayed(),
                "Most recent game (7 rounds) should appear first");
    }

    // Player deduplication

    @Test
    void saveGame_doesNotDuplicatePlayers() {
        repo.saveGame(List.of("Alice", "Bob"), new int[]{10, 0}, 0, 5, LocalDateTime.now());
        repo.saveGame(List.of("Alice", "Bob"), new int[]{10, 0}, 0, 5, LocalDateTime.now());

        try (SqlSession session = Database.factory().openSession()) {
            PlayerMapper pm = session.getMapper(PlayerMapper.class);
            assertNotNull(pm.findByName("Alice"),
                    "Alice should be findable after two games");
        }
    }

    // winCounts / topScores

    @Test
    void winCounts_aggregatesAcrossGames() {
        // Alice wins twice, Bob wins once
        repo.saveGame(List.of("Alice", "Bob"), new int[]{100, 0}, 0, 10, LocalDateTime.now());
        repo.saveGame(List.of("Alice", "Bob"), new int[]{80, 0}, 0, 8, LocalDateTime.now());
        repo.saveGame(List.of("Alice", "Bob"), new int[]{0, 60}, 1, 12, LocalDateTime.now());

        List<WinCountRecord> counts = repo.winCounts();
        assertFalse(counts.isEmpty());

        WinCountRecord top = counts.getFirst();
        assertEquals("Alice", top.getPlayerName(), "Alice should lead the win table");
        assertEquals(2, top.getWins());
    }

    @Test
    void topScores_aggregatesAcrossGames() {
        // Alice: 100+80 = 180 total; Bob: 50+70 = 120 total
        repo.saveGame(List.of("Alice", "Bob"), new int[]{100, 50}, 0, 10, LocalDateTime.now());
        repo.saveGame(List.of("Alice", "Bob"), new int[]{80, 70}, 0, 8, LocalDateTime.now());

        List<TopScoreRecord> scores = repo.topScores();
        assertFalse(scores.isEmpty());

        TopScoreRecord top = scores.getFirst();
        assertEquals("Alice", top.getPlayerName(), "Alice should lead the score table");
        assertEquals(180, top.getTotalScore());
    }
}
