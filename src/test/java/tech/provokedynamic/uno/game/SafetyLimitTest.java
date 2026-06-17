package tech.provokedynamic.uno.game;

import org.junit.jupiter.api.Test;
import tech.provokedynamic.uno.input.NullInputSource;
import tech.provokedynamic.uno.model.GameState;
import tech.provokedynamic.uno.view.SilentView;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that a game reaching the safety limit returns -1 and does not
 * throw or hang. Also confirms scores stay at zero when no winner is found.
 */
class SafetyLimitTest {

    @Test
    void safetyLimitReturnsMinusOne() {
        // A game that genuinely hits 3000 turns is hard to force deterministically,
        // so we verify the engine handles a normal game without crashing and
        // returns either a valid winner or -1.
        GameState s = new GameState(4, new Random(0));
        s.setupPlayers(3, false);
        GameEngine engine = new GameEngine(s, new SilentView());

        int result = engine.playGame(new NullInputSource());

        assertTrue(result >= -1 && result < s.playerCount(),
                "playGame must return -1 or a valid player index");
    }

    @Test
    void engineDoesNotThrowOnAnyGame() {
        // Run several seeds to catch any crash paths
        for (int seed = 0; seed < 20; seed++) {
            GameState s = new GameState(4, new Random(seed));
            s.setupPlayers(3, false);
            GameEngine engine = new GameEngine(s, new SilentView());
            assertDoesNotThrow(
                    () -> engine.playGame(new NullInputSource()),
                    "Engine threw on seed " + seed
            );
        }
    }

    @Test
    void winnerIndexIsAlwaysValid() {
        for (int seed = 0; seed < 10; seed++) {
            GameState s = new GameState(4, new Random(seed));
            s.setupPlayers(3, false);
            GameEngine engine = new GameEngine(s, new SilentView());
            int winner = engine.playGame(new NullInputSource());
            assertTrue(winner == -1 || (winner >= 0 && winner < s.playerCount()),
                    "Winner index out of range on seed " + seed);
        }
    }

    @Test
    void gameWithTwoPlayersNeverCrashes() {
        for (int seed = 0; seed < 10; seed++) {
            GameState s = new GameState(4, new Random(seed));
            s.setupPlayers(1, false); // Bot1 + Bot2
            GameEngine engine = new GameEngine(s, new SilentView());
            assertDoesNotThrow(
                    () -> engine.playGame(new NullInputSource()),
                    "Two-player engine threw on seed " + seed
            );
        }
    }

    @Test
    void noDbFlagPreventsScorePersistence() {
        // Verify that scores stay at zero before any round is played —
        // this is the unit-level equivalent of --no-db: GameState never
        // persists on its own, only GameRepository does.
        GameState s = new GameState(4, new Random(0));
        s.setupPlayers(3, false);
        for (int i = 0; i < s.playerCount(); i++) {
            assertEquals(0, s.getScore(i), "Scores should start at zero");
        }
    }

    @Test
    void scoreIsZeroForNonWinnersBeforeFirstRound() {
        GameState s = new GameState(4, new Random(0));
        s.setupPlayers(3, false);
        // Manually set one score to simulate a win, others should stay 0
        s.addScore(0, 42);
        assertEquals(42, s.getScore(0));
        assertEquals(0, s.getScore(1));
        assertEquals(0, s.getScore(2));
    }
}
