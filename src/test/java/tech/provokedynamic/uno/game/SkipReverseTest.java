package tech.provokedynamic.uno.game;

import org.junit.jupiter.api.Test;
import tech.provokedynamic.uno.model.Card;
import tech.provokedynamic.uno.model.GameState;
import tech.provokedynamic.uno.view.SilentView;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SkipReverseTest {

    private final SilentView view = new SilentView();

    private GameState threePlayer() {
        GameState s = new GameState(4, new Random(0));
        s.setupPlayers(3, false);         // Bot1, Bot2, Bot3
        s.buildAndShuffleDeck();
        s.setDirection(1);
        s.setCurrentPlayer(0);
        s.setUpCard(new Card(Card.Color.RED, Card.Rank.NUMBER, 1));
        s.setCalledColor(Card.Color.NONE);
        return s;
    }

    private GameState twoPlayer() {
        GameState s = new GameState(4, new Random(0));
        s.setupPlayers(1, true);          // You + Bot1
        s.buildAndShuffleDeck();
        s.setDirection(1);
        s.setCurrentPlayer(0);
        s.setUpCard(new Card(Card.Color.RED, Card.Rank.NUMBER, 1));
        s.setCalledColor(Card.Color.NONE);
        return s;
    }

    // --- Skip ---

    @Test
    void skipAdvancesOverNextPlayer() {
        GameState s = threePlayer();
        // current = 0; after skip, current should be 2 (player 1 skipped)
        CardEffects.forRank(Card.Rank.SKIP).apply(s, view);
        assertEquals(2, s.getCurrentPlayer());
    }

    @Test
    void skipInThreePlayerLeavesCorrectTurn() {
        GameState s = threePlayer();
        s.setCurrentPlayer(2);
        // current = 2; skip wraps: next would be 0, skip them → land on 1
        CardEffects.forRank(Card.Rank.SKIP).apply(s, view);
        assertEquals(1, s.getCurrentPlayer());
    }

    // --- Reverse ---

    @Test
    void reverseChangesDirectionClockwiseToCounter() {
        GameState s = threePlayer();
        assertEquals(1, s.getDirection());
        CardEffects.forRank(Card.Rank.REVERSE).apply(s, view);
        assertEquals(-1, s.getDirection());
    }

    @Test
    void reverseInThreePlayerAdvancesOneTurn() {
        GameState s = threePlayer();
        // current = 0, direction flips to -1, next() → (0 + (-1) + 3) % 3 = 2
        CardEffects.forRank(Card.Rank.REVERSE).apply(s, view);
        assertEquals(2, s.getCurrentPlayer());
    }

    @Test
    void reverseInTwoPlayerActsLikeSkip() {
        GameState s = twoPlayer();
        // current = 0; reverse flips direction then double-next → stays at 0
        CardEffects.forRank(Card.Rank.REVERSE).apply(s, view);
        assertEquals(0, s.getCurrentPlayer());
    }

    @Test
    void reverseInTwoPlayerChangesDirection() {
        GameState s = twoPlayer();
        CardEffects.forRank(Card.Rank.REVERSE).apply(s, view);
        assertEquals(-1, s.getDirection());
    }
}
