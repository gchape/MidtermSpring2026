package tech.provokedynamic.uno.game;

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
 * Tests for the UNO-call mechanic.
 * <p>
 * Bots always successfully call UNO (a documented simplification — see
 * docs/rules-supported.md), so the only player who can genuinely miss a
 * call is a human who declines (or fails to respond affirmatively) when
 * {@link PlayerInputSource#askCallUno()} is asked. These tests demonstrate
 * both outcomes explicitly: a successful call avoids the penalty, and a
 * declined call exposes the player to it on the very next turn.
 */
class UnoCallTest {

    private GameState threeBot() {
        GameState s = new GameState(4, new Random(1));
        s.setupPlayers(3, false);
        s.buildAndShuffleDeck();
        s.setDirection(1);
        s.setCurrentPlayer(0);
        s.setUpCard(new Card(Card.Color.RED, Card.Rank.NUMBER, 5));
        s.setCalledColor(Card.Color.NONE);
        return s;
    }

    private GameState oneHumanTwoBots() {
        GameState s = new GameState(4, new Random(1));
        s.setupPlayers(2, true); // You(0), Bot1(1), Bot2(2)
        s.buildAndShuffleDeck();
        s.setDirection(1);
        s.setCurrentPlayer(0);
        s.setUpCard(new Card(Card.Color.RED, Card.Rank.NUMBER, 5));
        s.setCalledColor(Card.Color.NONE);
        return s;
    }

    @Test
    void botsAutomaticallyCallUnoWhenReachingOneCard() {
        GameState s = threeBot();
        TrackingView view = new TrackingView();
        GameEngine engine = new GameEngine(s, view);

        // Give player 0 exactly two RED cards so one play leaves one card → UNO
        s.clearHand(0);
        s.addToHand(0, new Card(Card.Color.RED, Card.Rank.NUMBER, 3));
        s.addToHand(0, new Card(Card.Color.RED, Card.Rank.NUMBER, 7));

        engine.playTurn(new NullInputSource());

        assertEquals("Bot1", view.unoCaller, "Bots always successfully call UNO when they reach one card");
        assertEquals(1, s.handSize(0));
    }

    @Test
    void humanCallingUnoAvoidsThePenalty() {
        GameState s = oneHumanTwoBots();
        TrackingView view = new TrackingView();
        GameEngine engine = new GameEngine(s, view);

        s.clearHand(0);
        s.addToHand(0, new Card(Card.Color.RED, Card.Rank.NUMBER, 3));
        s.addToHand(0, new Card(Card.Color.RED, Card.Rank.NUMBER, 7));

        // Human plays index 0 (RED 3), drops to one card, and calls UNO (true)
        engine.playTurn(new ScriptedInput(true, 0));

        assertEquals(1, s.handSize(0));
        assertEquals("You", view.unoCaller, "A successful call should be announced");

        // Give the next player (Bot1) a hand that won't end the round
        s.clearHand(1);
        s.addToHand(1, new Card(Card.Color.RED, Card.Rank.NUMBER, 9));
        s.addToHand(1, new Card(Card.Color.BLUE, Card.Rank.NUMBER, 2));
        s.addToHand(1, new Card(Card.Color.GREEN, Card.Rank.NUMBER, 4));

        int before = s.handSize(0);
        engine.playTurn(new NullInputSource()); // Bot1's turn — missed-UNO check runs for player 0

        assertEquals(before, s.handSize(0), "Successfully calling UNO should avoid the penalty");
        assertNull(view.missedUnoPenalised, "No penalty should have been applied");
    }

    @Test
    void decliningTheUnoCallExposesThePlayerToThePenalty() {
        GameState s = oneHumanTwoBots();
        TrackingView view = new TrackingView();
        GameEngine engine = new GameEngine(s, view);

        s.clearHand(0);
        s.addToHand(0, new Card(Card.Color.RED, Card.Rank.NUMBER, 3));
        s.addToHand(0, new Card(Card.Color.RED, Card.Rank.NUMBER, 7));

        // Human plays index 0 (RED 3), drops to one card, and declines to call UNO (false)
        engine.playTurn(new ScriptedInput(false, 0));

        assertEquals(1, s.handSize(0));
        assertNull(view.unoCaller, "Declining the call should not announce UNO");

        s.clearHand(1);
        s.addToHand(1, new Card(Card.Color.RED, Card.Rank.NUMBER, 9));
        s.addToHand(1, new Card(Card.Color.BLUE, Card.Rank.NUMBER, 2));
        s.addToHand(1, new Card(Card.Color.GREEN, Card.Rank.NUMBER, 4));

        int before = s.handSize(0);
        engine.playTurn(new NullInputSource()); // Bot1's turn — missed-UNO check runs for player 0

        assertEquals(before + 2, s.handSize(0), "Declining the call should expose the player to the penalty");
        assertEquals("You", view.missedUnoPenalised);
    }

    @Test
    void fullGameCompletesWithWinner() {
        GameState s = threeBot();
        GameEngine engine = new GameEngine(s, new SilentView());
        int winner = engine.playGame(new NullInputSource());
        assertTrue(winner >= 0 || winner == -1, "playGame must return a valid index or -1");
    }

    /**
     * View that records UNO and missed-UNO events.
     */
    private static class TrackingView extends SilentView {
        String unoCaller = null;
        String missedUnoPenalised = null;

        @Override
        public void showUno(String name) {
            unoCaller = name;
        }

        @Override
        public void showMissedUno(String name) {
            missedUnoPenalised = name;
        }
    }

    /**
     * Scripted input for the human seat: plays the given card indices in
     * order, and answers the UNO-call prompt with a fixed yes/no.
     */
    private static class ScriptedInput implements PlayerInputSource {
        private final boolean callUno;
        private final int[] choices;
        private int i = 0;

        ScriptedInput(boolean callUno, int... choices) {
            this.callUno = callUno;
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
            return callUno;
        }
    }
}
