package tech.provokedynamic.uno.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.provokedynamic.uno.input.NullInputSource;
import tech.provokedynamic.uno.input.PlayerInputSource;
import tech.provokedynamic.uno.model.Card;
import tech.provokedynamic.uno.model.GameState;
import tech.provokedynamic.uno.view.SilentView;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Characterization tests for GameEngine.
 * <p>
 * Two categories:
 * <p>
 * 1. Integration tests — run full bot-only games and verify high-level outcomes:
 * a winner is produced, scores accumulate, and the safety limit is not hit.
 * <p>
 * 2. Quirk tests — characterize the specific edge-case behaviors documented
 * in the original implementation that must be preserved across refactoring:
 * - Illegal card index causes a penalty card and turn loss (not a crash).
 * - Illegal card causes a penalty card and turn loss.
 * - Bot automatically plays a drawn card when it is legal.
 * <p>
 * All tests use SilentView — no console output.
 */
class GameEngineTest {

    private GameState state;
    private GameEngine engine;

    @BeforeEach
    void setUp() {
        state = new GameState(10, new Random(42));
        state.setupPlayers(3, false);
        engine = new GameEngine(state, new SilentView());
    }

    @Test
    void botOnlyGameProducesWinner() {
        int winner = engine.playGame(new NullInputSource());

        assertTrue(winner >= 0 && winner < state.playerCount(),
                "Expected a valid winner index, got: " + winner);
    }

    @Test
    void winnerScoreIsPositiveAfterGame() {
        int winner = engine.playGame(new NullInputSource());

        assertTrue(state.getScore(winner) > 0,
                "Winner should score at least some points");
    }

    @Test
    void scoreAccumulatesAcrossMultipleGames() {
        engine.playGame(new NullInputSource());
        engine.playGame(new NullInputSource());

        int total = 0;
        for (int i = 0; i < state.playerCount(); i++) {
            total += state.getScore(i);
        }

        assertTrue(total > 0, "Total score across all players should be positive after 2 games");
    }

    @Test
    void twoPlayerGameCompletes() {
        GameState s = new GameState(10, new Random(42));
        s.setupPlayers(2, false);

        int winner = new GameEngine(s, new SilentView()).playGame(new NullInputSource());

        assertTrue(winner >= 0 && winner < s.playerCount());
    }

    @Test
    void fourPlayerGameCompletes() {
        GameState s = new GameState(10, new Random(42));
        s.setupPlayers(4, false);

        int winner = new GameEngine(s, new SilentView()).playGame(new NullInputSource());

        assertTrue(winner >= 0 && winner < s.playerCount());
    }

    @Test
    void deterministicWinnerWithFixedSeed() {
        GameState s1 = new GameState(10, new Random(99));
        s1.setupPlayers(3, false);

        int first = new GameEngine(s1, new SilentView()).playGame(new NullInputSource());

        GameState s2 = new GameState(10, new Random(99));
        s2.setupPlayers(3, false);

        int second = new GameEngine(s2, new SilentView()).playGame(new NullInputSource());

        assertEquals(first, second, "Same seed must always produce the same winner");
    }

    @Test
    void illegalIndexCausesPenaltyCardAndTurnLoss() {
        GameState s = new GameState(10, new Random(42));
        s.setupPlayers(1, true); // You + Bot1

        GameEngine e = new GameEngine(s, new SilentView());
        e.startGame();

        s.setCurrentPlayer(0); // human is always index 0
        int before = s.handSize(0);

        // index 99 is always out of range for a 7-card hand
        e.playTurn(new ScriptedInput(99));

        assertEquals(before + 1, s.handSize(0),
                "Out-of-range index should add one penalty card");
        assertNotEquals(0, s.getCurrentPlayer(),
                "Turn should advance after illegal index");
    }

    @Test
    void illegalCardCausesPenaltyCardAndTurnLoss() {
        GameState s = new GameState(10, new Random(42));
        s.setupPlayers(1, true);

        GameEngine e = new GameEngine(s, new SilentView());
        e.startGame();

        s.setCurrentPlayer(0);

        // Replace hand with a single card that is definitely illegal on the up-card
        while (s.handSize(0) > 0) {
            s.removeFromHand(0, 0);
        }
        s.addToHand(0, illegalCardFor(s.getUpCard()));

        // plays index 0, which is illegal
        e.playTurn(new ScriptedInput(0));

        assertEquals(2, s.handSize(0),
                "Illegal card should leave one original card plus one penalty card");
        assertNotEquals(0, s.getCurrentPlayer(),
                "Turn should advance after illegal card");
    }

    /**
     * Original behavior: when a bot draws and the drawn card is legal,
     * it immediately plays it without any confirmation.
     * <p>
     * Unlike the previous version of this test, this one drives the real
     * {@code GameEngine.playTurn()} / {@code handleDraw()} code path: the
     * bot's hand is forced to a single illegal card (so it must draw), and
     * {@code GameState.forceNextDraw()} guarantees the next draw is legal.
     * The assertions then confirm the engine actually played that card,
     * rather than just checking arithmetic on hand sizes.
     */
    @Test
    void botAutoPlaysDrawnCardWhenLegal() {
        engine.startGame();

        int cp = state.getCurrentPlayer();
        Card upCard = state.getUpCard();

        // Give the bot a hand with only one illegal card — forces a draw.
        while (state.handSize(cp) > 0) {
            state.removeFromHand(cp, 0);
        }
        state.addToHand(cp, illegalCardFor(upCard));

        // Rig the deck so the very next draw is guaranteed legal.
        Card legalCard = legalCardFor(upCard);
        state.forceNextDraw(legalCard);

        int handSizeBefore = state.handSize(cp); // 1 (the illegal card)

        engine.playTurn(new NullInputSource());

        assertEquals(legalCard.toString(), state.getUpCard().toString(),
                "Bot should have immediately played the legal card it drew");
        assertEquals(handSizeBefore, state.handSize(cp),
                "Net hand size should be unchanged: +1 from the draw, -1 from the auto-play");
    }

    private Card illegalCardFor(Card upCard) {
        Card.Color other = (upCard.color() == Card.Color.RED || upCard.color() == Card.Color.NONE)
                ? Card.Color.BLUE : Card.Color.RED;

        int safeNumber = (upCard.rank() == Card.Rank.NUMBER && upCard.number() == 3) ? 7 : 3;

        return new Card(other, Card.Rank.NUMBER, safeNumber);
    }

    private Card legalCardFor(Card upCard) {
        if (upCard.isWild() || upCard.color() == Card.Color.NONE) {
            return new Card(Card.Color.NONE, Card.Rank.WILD, -1);
        }

        int safeNumber = (upCard.rank() == Card.Rank.NUMBER && upCard.number() == 1) ? 2 : 1;

        return new Card(upCard.color(), Card.Rank.NUMBER, safeNumber);
    }

    /**
     * Scripted input for human-turn tests. Returns card choices in order,
     * then -1 (draw) once exhausted. askCallUno() defaults to true since
     * these tests are not exercising the UNO-call mechanic — see
     * UnoCallTest for that.
     */
    private static class ScriptedInput implements PlayerInputSource {
        private final int[] choices;
        private int i = 0;

        ScriptedInput(int... choices) {
            this.choices = choices;
        }

        @Override
        public int askHuman(List<Card> hand, Card upCard, Card.Color c) {
            return i < choices.length ? choices[i++] : -1;
        }

        @Override
        public boolean askPlayDrawn(Card drawn) {
            return false;
        }

        @Override
        public Card.Color askColor() {
            return Card.Color.RED;
        }

        @Override
        public boolean askCallUno() {
            return true;
        }
    }
}
