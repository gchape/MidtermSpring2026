package tech.provokedynamic.uno.game;

import org.junit.jupiter.api.Test;
import tech.provokedynamic.uno.input.NullInputSource;
import tech.provokedynamic.uno.model.GameState;
import tech.provokedynamic.uno.view.SilentView;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link GameRunner}.
 * <p>
 * Verifies that the multi-round match loop terminates correctly, detects the
 * target-score winner, accumulates scores across rounds, and handles the
 * no-persistence path (repo == null) without crashing.
 * <p>
 * No console output — SilentView and NullInputSource throughout.
 */
class GameRunnerTest {

    private GameState botState(long seed) {
        GameState s = new GameState(10, new Random(seed));
        s.setupPlayers(3, false);
        return s;
    }

    @Test
    void runTerminatesWithoutException() {
        GameState s = botState(42);
        assertDoesNotThrow(() ->
                new GameRunner(s, new SilentView(), null, 500, true)
                        .run(new NullInputSource())
        );
    }

    @Test
    void winnerReachesTargetScore() {
        int target = 200;
        GameState s = botState(7);

        new GameRunner(s, new SilentView(), null, target, true)
                .run(new NullInputSource());

        int maxScore = 0;
        for (int i = 0; i < s.playerCount(); i++) {
            maxScore = Math.max(maxScore, s.getScore(i));
        }

        assertTrue(maxScore >= target,
                "At least one player should reach the target score of " + target);
    }

    @Test
    void scoresArePositiveAfterMatch() {
        GameState s = botState(13);

        new GameRunner(s, new SilentView(), null, 300, true)
                .run(new NullInputSource());

        int total = 0;
        for (int i = 0; i < s.playerCount(); i++) total += s.getScore(i);

        assertTrue(total > 0, "Total score across all players should be positive after a match");
    }

    @Test
    void noDbPathDoesNotCrash() {
        // repo == null simulates --no-db; runner must not NPE at persist step
        GameState s = botState(1);
        assertDoesNotThrow(() ->
                new GameRunner(s, new SilentView(), null, 200, true)
                        .run(new NullInputSource())
        );
    }

    @Test
    void deterministicResultWithFixedSeed() {
        int target = 300;

        GameState s1 = botState(55);
        new GameRunner(s1, new SilentView(), null, target, true).run(new NullInputSource());

        GameState s2 = botState(55);
        new GameRunner(s2, new SilentView(), null, target, true).run(new NullInputSource());

        for (int i = 0; i < s1.playerCount(); i++) {
            assertEquals(s1.getScore(i), s2.getScore(i),
                    "Same seed should produce identical final scores");
        }
    }

    @Test
    void twoPlayerMatchTerminates() {
        GameState s = new GameState(10, new Random(3));
        s.setupPlayers(2, false); // Bot1 + Bot2 + Bot3 = 3 players, fast game
        assertDoesNotThrow(() ->
                new GameRunner(s, new SilentView(), null, 200, true)
                        .run(new NullInputSource())
        );
    }

    @Test
    void lowTargetFinishesQuickly() {
        // Target of 1 should finish in one round — sanity check that the
        // loop exits as soon as any score >= target, not just at exactly target
        GameState s = botState(99);

        new GameRunner(s, new SilentView(), null, 1, true)
                .run(new NullInputSource());

        int maxScore = 0;
        for (int i = 0; i < s.playerCount(); i++) {
            maxScore = Math.max(maxScore, s.getScore(i));
        }

        assertTrue(maxScore >= 1, "With target=1 someone should score at least 1 point");
    }
}
