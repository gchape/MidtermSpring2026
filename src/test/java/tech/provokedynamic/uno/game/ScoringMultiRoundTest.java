package tech.provokedynamic.uno.game;

import org.junit.jupiter.api.Test;
import tech.provokedynamic.uno.input.NullInputSource;
import tech.provokedynamic.uno.model.Card;
import tech.provokedynamic.uno.model.GameState;
import tech.provokedynamic.uno.rules.Rules;
import tech.provokedynamic.uno.view.SilentView;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScoringMultiRoundTest {

    // --- Round scoring (1.10 behavior) ---

    @Test
    void winnerReceivesOpponentHandPoints() {
        GameState s = new GameState(4, new Random(0));
        s.setupPlayers(2, false);
        s.buildAndShuffleDeck();
        s.setDirection(1);
        s.setCurrentPlayer(0);
        s.setUpCard(new Card(Card.Color.RED, Card.Rank.NUMBER, 5));
        s.setCalledColor(Card.Color.NONE);

        // Give player 0 exactly one card matching the up-card color so they can win immediately
        s.clearHand(0);
        s.addToHand(0, new Card(Card.Color.RED, Card.Rank.NUMBER, 9));

        // Give player 1 known cards to compute expected score
        s.clearHand(1);
        s.addToHand(1, new Card(Card.Color.BLUE, Card.Rank.NUMBER, 5));   // 5 pts
        s.addToHand(1, new Card(Card.Color.GREEN, Card.Rank.SKIP, -1));   // 20 pts
        int expectedPoints = 25;

        GameEngine engine = new GameEngine(s, new SilentView());
        engine.playTurn(new NullInputSource());

        assertEquals(expectedPoints, s.getScore(0), "Winner should collect opponent hand points");
    }

    @Test
    void handPointsCalculation() {
        List<Card> hand = List.of(
                new Card(Card.Color.RED, Card.Rank.NUMBER, 7),
                new Card(Card.Color.BLUE, Card.Rank.SKIP, -1),
                new Card(Card.Color.NONE, Card.Rank.WILD, -1)
        );
        assertEquals(7 + 20 + 50, Rules.handPoints(hand));
    }

    @Test
    void scoresAccumulateAcrossRounds() {
        GameState s = new GameState(4, new Random(7));
        s.setupPlayers(3, false);
        GameEngine engine = new GameEngine(s, new SilentView());

        // Play two full rounds and check scores only go up
        engine.playGame(new NullInputSource());
        int[] afterRound1 = new int[s.playerCount()];
        for (int i = 0; i < s.playerCount(); i++) afterRound1[i] = s.getScore(i);

        engine.playGame(new NullInputSource());
        for (int i = 0; i < s.playerCount(); i++) {
            assertTrue(s.getScore(i) >= afterRound1[i], "Scores should only increase across rounds");
        }
    }

    @Test
    void targetScoreDetectedAfterRound() {
        // Simulate the Main loop logic: play rounds until someone hits 500
        int target = 500;
        GameState s = new GameState(4, new Random(99));
        s.setupPlayers(3, false);
        GameEngine engine = new GameEngine(s, new SilentView());

        int overallWinner = -1;
        int rounds = 0;

        while (overallWinner == -1 && rounds < 100) {
            rounds++;
            engine.playGame(new NullInputSource());
            for (int i = 0; i < s.playerCount(); i++) {
                if (s.getScore(i) >= target) {
                    overallWinner = i;
                    break;
                }
            }
        }

        assertTrue(overallWinner >= 0, "A winner should be found within 100 rounds");
        assertTrue(s.getScore(overallWinner) >= target, "Winner's score should meet the target");
    }
}
