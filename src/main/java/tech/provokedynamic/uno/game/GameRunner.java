package tech.provokedynamic.uno.game;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tech.provokedynamic.uno.db.repository.GameRepository;
import tech.provokedynamic.uno.input.PlayerInputSource;
import tech.provokedynamic.uno.model.GameState;
import tech.provokedynamic.uno.view.GameView;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Drives the multi-round match loop: plays rounds until a player reaches
 * the target score, then persists the result and prints the final outcome.
 * <p>
 * Separated from {@code Main} so the loop is testable without touching the
 * CLI argument parser.
 */
@Slf4j
@RequiredArgsConstructor
public class GameRunner {

    private final GameState state;
    private final GameView view;
    private final GameRepository repo;   // null when --no-db
    private final int target;
    private final boolean quiet;

    /**
     * Runs rounds until one player reaches {@link #target}, then persists
     * and announces the winner.
     */
    public void run(PlayerInputSource input) {
        GameEngine engine = new GameEngine(state, view);

        LocalDateTime startedAt = LocalDateTime.now();
        int round = 0;
        int overallWinner = -1;

        log.info("Match started: players={}, target={}", state.playerCount(), target);

        while (overallWinner == -1) {
            round++;
            log.info("=== Round {} ===", round);
            if (!quiet) IO.println("\n=== Round " + round + " ===");

            engine.playGame(input);

            if (!quiet) printRoundScores(round);

            overallWinner = findWinner();
        }

        log.info("Match over after {} round(s) — winner: {} with {} points",
                round, state.playerName(overallWinner), state.getScore(overallWinner));

        printMatchResult(overallWinner);

        if (repo != null) {
            persist(overallWinner, round, startedAt);
        } else {
            log.debug("Persistence skipped (--no-db)");
        }
    }

    private void printRoundScores(int round) {
        IO.println("\nScores after round " + round + ":");
        for (int i = 0; i < state.playerCount(); i++) {
            IO.println("  " + state.playerName(i) + ": " + state.getScore(i));
        }
    }

    private int findWinner() {
        for (int i = 0; i < state.playerCount(); i++) {
            if (state.getScore(i) >= target) return i;
        }
        return -1;
    }

    private void printMatchResult(int winner) {
        IO.println("\n" + state.playerName(winner)
                + " wins the match with " + state.getScore(winner) + " points!");
        IO.println("\nFinal scores:");
        for (int i = 0; i < state.playerCount(); i++) {
            IO.println("  " + state.playerName(i) + ": " + state.getScore(i));
        }
    }

    private void persist(int winner, int rounds, LocalDateTime startedAt) {
        log.info("Persisting match: rounds={}, winner={}", rounds, state.playerName(winner));
        List<String> names = new ArrayList<>();
        int[] scores = new int[state.playerCount()];
        for (int i = 0; i < state.playerCount(); i++) {
            names.add(state.playerName(i));
            scores[i] = state.getScore(i);
        }
        repo.saveGame(names, scores, winner, rounds, startedAt);
        log.info("Match persisted successfully");
    }
}
