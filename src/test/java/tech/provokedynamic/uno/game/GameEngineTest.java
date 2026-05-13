package tech.provokedynamic.uno.game;

import org.junit.jupiter.api.Test;
import tech.provokedynamic.uno.model.GameState;
import tech.provokedynamic.uno.view.SilentView;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration characterization tests for GameEngine.
 * <p>
 * These tests run full bot-only games and verify high-level outcomes:
 * a winner is produced, scores accumulate, and the safety limit is not hit
 * under normal conditions.
 * <p>
 * No console I/O — SilentView suppresses all output.
 */
class GameEngineTest {

    @Test
    void botOnlyGameProducesWinner() {
        GameState state = new GameState(10, new Random(42));
        state.setupPlayers(3, false);
        GameEngine engine = new GameEngine(state, new SilentView());

        int winner = engine.playGame(null);

        assertTrue(winner >= 0 && winner < state.playerCount(),
                "Expected a valid winner index, got: " + winner);
    }

    @Test
    void winnerScoreIsPositiveAfterGame() {
        GameState state = new GameState(10, new Random(42));
        state.setupPlayers(3, false);
        GameEngine engine = new GameEngine(state, new SilentView());

        int winner = engine.playGame(null);

        assertTrue(state.getScore(winner) > 0,
                "Winner should score at least some points");
    }

    @Test
    void scoreAccumulatesAcrossMultipleGames() {
        GameState state = new GameState(10, new Random(7));
        state.setupPlayers(3, false);
        GameEngine engine = new GameEngine(state, new SilentView());

        engine.playGame(null);
        engine.playGame(null);

        int total = 0;
        for (int i = 0; i < state.playerCount(); i++) total += state.getScore(i);
        assertTrue(total > 0, "Total score across all players should be positive after 2 games");
    }

    @Test
    void twoPlayerGameCompletes() {
        GameState state = new GameState(10, new Random(42));
        state.setupPlayers(2, false);
        GameEngine engine = new GameEngine(state, new SilentView());

        int winner = engine.playGame(null);

        assertTrue(winner >= 0 && winner < state.playerCount());
    }

    @Test
    void fourPlayerGameCompletes() {
        GameState state = new GameState(10, new Random(42));
        state.setupPlayers(4, false);
        GameEngine engine = new GameEngine(state, new SilentView());

        int winner = engine.playGame(null);

        assertTrue(winner >= 0 && winner < state.playerCount());
    }
}