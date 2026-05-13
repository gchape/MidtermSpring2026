package tech.provokedynamic.uno.model;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Characterization tests for GameState.
 * <p>
 * Covers deck draw, discard reshuffle, empty-deck fallback, hand mutation,
 * and turn navigation (skip, reverse, draw-two effects on currentPlayer).
 * No game engine or console involved.
 */
class GameStateTest {

    @Test
    void drawFromEmptyDeckReshufflesDiscard() {
        GameState state = freshState();
        while (state.deckSize() > 0) state.draw();

        state.addToDiscard(new Card("R5"));
        state.addToDiscard(new Card("G3"));

        Card drawn = state.draw();
        assertNotNull(drawn);
        assertEquals(0, state.discardSize()); // discard was consumed
    }

    @Test
    void drawFromCompletelyEmptyReturnsWild() {
        // With no deck and no discard, draw() returns the fallback "W"
        GameState state = new GameState(10, new Random(0));
        state.setupPlayers(1, false);
        Card last = null;
        for (int i = 0; i < 200; i++) {
            last = state.draw();
        }
        assertEquals("W", last.code());
    }

    @Test
    void addToHandIncreasesHandSize() {
        GameState state = freshState();
        int before = state.handSize(0);
        state.addToHand(0, new Card("R5"));
        assertEquals(before + 1, state.handSize(0));
    }

    @Test
    void removeFromHandDecreasesHandSize() {
        GameState state = freshState();
        state.addToHand(0, new Card("G3"));
        int before = state.handSize(0);
        state.removeFromHand(0, 0);
        assertEquals(before - 1, state.handSize(0));
    }

    @Test
    void getFromHandReturnsCorrectCard() {
        GameState state = freshState();
        state.addToHand(0, new Card("Y7"));
        Card last = state.getFromHand(0, state.handSize(0) - 1);
        assertEquals("Y7", last.code());
    }

    @Test
    void skipAdvancesTwoPlayersForward() {
        GameState state = freshThreePlayerState();
        state.setCurrentPlayer(0);
        state.setDirection(1);
        state.next();
        state.next();
        assertEquals(2, state.getCurrentPlayer());
    }

    @Test
    void skipWrapsAroundCorrectly() {
        GameState state = freshThreePlayerState();
        state.setCurrentPlayer(2);
        state.setDirection(1);
        state.next();
        state.next();
        assertEquals(1, state.getCurrentPlayer());
    }

    @Test
    void reverseFlipsDirectionPositiveToNegative() {
        GameState state = freshThreePlayerState();
        assertEquals(1, state.getDirection());
        state.setDirection(state.getDirection() * -1);
        assertEquals(-1, state.getDirection());
    }

    @Test
    void reverseInTwoPlayerActsLikeSkip() {
        // Original quirk: 2-player reverse calls next() twice, returning to same player
        GameState state = freshState();
        state.setCurrentPlayer(0);
        state.setDirection(1);
        state.setDirection(state.getDirection() * -1); // flip
        state.next();
        state.next();
        assertEquals(0, state.getCurrentPlayer());
    }

    @Test
    void drawTwoGivesNextPlayerTwoCards() {
        GameState state = freshThreePlayerState();
        state.addToDiscard(new Card("R1"));
        state.addToDiscard(new Card("R2"));

        int nextPlayer = 1;
        int before = state.handSize(nextPlayer);
        state.addToHand(nextPlayer, state.draw());
        state.addToHand(nextPlayer, state.draw());
        assertEquals(before + 2, state.handSize(nextPlayer));
    }

    private GameState freshState() {
        GameState state = new GameState(10, new Random(0));
        state.setupPlayers(2, false); // playerCount bots, no human
        for (int i = 0; i < 2; i++) {
            state.addToHand(i, new Card("R1"));
        }
        state.setUpCard(new Card("R9"));
        state.setCalledColor("");
        return state;
    }

    private GameState freshThreePlayerState() {
        GameState state = new GameState(10, new Random(0));
        state.setupPlayers(3, false);
        for (int i = 0; i < 3; i++) state.addToHand(i, new Card("R1"));
        state.setUpCard(new Card("R9"));
        state.setCalledColor("");
        return state;
    }
}