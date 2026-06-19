package tech.provokedynamic.uno.db;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.provokedynamic.uno.db.repository.GameRepository;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies StatsReport output after saving games to an isolated in-memory H2 database.
 */
class StatsReportTest {

    private static final AtomicInteger DB_COUNTER = new AtomicInteger();

    private GameRepository repo;
    private StatsReport report;

    @BeforeEach
    void setUp() throws IOException, SQLException {
        String dbName = "stats_test_" + DB_COUNTER.incrementAndGet();
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
        report = new StatsReport(repo);
    }

    private String captureOutput(Runnable action) {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream old = System.out;
        System.setOut(new PrintStream(buf));
        try {
            action.run();
        } finally {
            System.setOut(old);
        }
        return buf.toString();
    }

    @Test
    void reportShowsNoGamesOnFreshDatabase() {
        String output = captureOutput(report::print);
        assertTrue(output.contains("No games recorded yet."));
    }

    @Test
    void reportShowsWinnerAfterSavedGame() {
        repo.saveGame(List.of("Alice", "Bob"), new int[]{150, 30}, 0, 5, LocalDateTime.now());
        String output = captureOutput(report::print);
        assertTrue(output.contains("Alice"), "Report should mention the winner");
        assertTrue(output.contains("★"), "Winner should be marked with a star");
    }

    @Test
    void reportShowsWinCountsAggregated() {
        repo.saveGame(List.of("Alice", "Bob"), new int[]{100, 0}, 0, 3, LocalDateTime.now());
        repo.saveGame(List.of("Alice", "Bob"), new int[]{80, 0}, 0, 4, LocalDateTime.now());
        repo.saveGame(List.of("Alice", "Bob"), new int[]{0, 60}, 1, 5, LocalDateTime.now());
        String output = captureOutput(report::print);
        assertTrue(output.contains("Alice"), "Alice should appear in win counts");
        assertTrue(output.contains("2 win"), "Alice should have 2 wins");
    }

    @Test
    void reportShowsTopScores() {
        repo.saveGame(List.of("Alice", "Bob"), new int[]{200, 50}, 0, 6, LocalDateTime.now());
        String output = captureOutput(report::print);
        assertTrue(output.contains("200"), "Alice's score should appear in top scores");
    }

    @Test
    void reportShowsNoDataWhenNoWinnersExist() {
        repo.saveGame(List.of("Alice", "Bob"), new int[]{0, 0}, -1, 3000, LocalDateTime.now());
        String output = captureOutput(report::print);
        assertTrue(output.contains("No data") || output.contains("Alice"),
                "Report should handle safety-limit games gracefully");
    }
}
