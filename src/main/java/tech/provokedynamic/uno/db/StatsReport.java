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
 * Invoked via the {@code --report} flag in {@code Main}.
 */
@RequiredArgsConstructor
public class StatsReport {

    private static final int RECENT_GAMES_LIMIT = 10;

    private final GameRepository repo;

    /**
     * Prints all three report sections to stdout.
     */
    public void print() {
        printRecentGames();
        printWinCounts();
        printTopScores();
    }

    private void printRecentGames() {
        List<GameRecord> games = repo.recentGames(RECENT_GAMES_LIMIT);
        System.out.printf("%n=== Recent Games (last %d) ===%n", RECENT_GAMES_LIMIT);

        if (games.isEmpty()) {
            System.out.println("  No games recorded yet.");
            return;
        }

        for (GameRecord g : games) {
            System.out.printf("  Game #%-4d  started=%-20s  rounds=%d%n",
                    g.getId(), g.getStartedAt(), g.getRoundsPlayed());
            for (GamePlayerRecord gp : g.getPlayers()) {
                String winner = gp.isWinner() ? " ★" : "";   // ★
                System.out.printf("    %-14s  score=%d%s%n",
                        gp.getPlayerName(), gp.getScore(), winner);
            }
        }
    }

    private void printWinCounts() {
        List<WinCountRecord> rows = repo.winCounts();
        System.out.printf("%n=== Player Win Counts ===%n");

        if (rows.isEmpty()) {
            System.out.println("  No data.");
            return;
        }

        for (WinCountRecord r : rows) {
            System.out.printf("  %-14s  %d win(s)%n", r.getPlayerName(), r.getWins());
        }
    }

    private void printTopScores() {
        List<TopScoreRecord> rows = repo.topScores();
        System.out.printf("%n=== Highest Total Scores ===%n");

        if (rows.isEmpty()) {
            System.out.println("  No data.");
            return;
        }

        for (TopScoreRecord r : rows) {
            System.out.printf("  %-14s  %d pts%n", r.getPlayerName(), r.getTotalScore());
        }
    }
}
