package tech.provokedynamic.uno.db;

import lombok.RequiredArgsConstructor;
import tech.provokedynamic.uno.db.model.GamePlayerRecord;
import tech.provokedynamic.uno.db.model.GameRecord;
import tech.provokedynamic.uno.db.model.TopScoreRecord;
import tech.provokedynamic.uno.db.model.WinCountRecord;
import tech.provokedynamic.uno.db.repository.GameRepository;

import java.util.List;

/**
 * Prints game history and player statistics to stdout.
 * Exposed via --report flag in Main.
 */
@RequiredArgsConstructor
public class StatsReport {

    private final GameRepository repo;

    public void print() {
        printRecentGames(10);
        printWinCounts();
        printTopScores();
    }

    private void printRecentGames(int limit) {
        List<GameRecord> games = repo.recentGames(limit);
        System.out.println("\n=== Recent Games (last " + limit + ") ===");
        if (games.isEmpty()) {
            System.out.println("  No games recorded yet.");
            return;
        }
        for (GameRecord g : games) {
            System.out.printf("  Game #%d  started=%s  rounds=%d%n",
                    g.getId(), g.getStartedAt(), g.getRoundsPlayed());
            for (GamePlayerRecord gp : g.getPlayers()) {
                String marker = gp.isWinner() ? " ★" : "";
                System.out.printf("    %-12s  score=%d%s%n",
                        gp.getPlayerName(), gp.getScore(), marker);
            }
        }
    }

    private void printWinCounts() {
        List<WinCountRecord> rows = repo.winCounts();
        System.out.println("\n=== Player Win Counts ===");
        if (rows.isEmpty()) {
            System.out.println("  No data.");
            return;
        }
        for (WinCountRecord r : rows) {
            System.out.printf("  %-12s  %d win(s)%n", r.getPlayerName(), r.getWins());
        }
    }

    private void printTopScores() {
        List<TopScoreRecord> rows = repo.topScores();
        System.out.println("\n=== Highest Total Scores ===");
        if (rows.isEmpty()) {
            System.out.println("  No data.");
            return;
        }
        for (TopScoreRecord r : rows) {
            System.out.printf("  %-12s  %d pts%n", r.getPlayerName(), r.getTotalScore());
        }
    }
}
