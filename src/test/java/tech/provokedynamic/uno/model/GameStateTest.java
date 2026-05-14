package tech.provokedynamic.uno.model;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Characterization tests for GameState.
 * <p>
 * Covers deck draw, discard reshuffle, empty-deck fallback, hand mutation,
 * scoring, and turn navigation (skip, reverse, draw-two effects on
 * currentPlayer). No game engine or console involved — pure state verification.
 */
class GameStateTest {

    @Test
    void drawFromEmptyDeckReshufflesDiscard() {
        GameState state = freshTwoPlayerState();
        while (state.deckSize() > 0) state.draw();

        state.addToDiscard(Card.fromCode("R5"));
        state.addToDiscard(Card.fromCode("G3"));

        Card drawn = state.draw();
        assertNotNull(drawn);
        assertEquals(0, state.discardSize(), "Discard pile should be consumed after reshuffle");
    }

    @Test
    void drawFromCompletelyEmptyDeckReturnsWild() {
        // With no deck and no discard, draw() returns the fallback wild card
        GameState state = new GameState(10, new Random(0));
        state.setupPlayers(1, false);
        // buildAndShuffleDeck is never called — deck starts empty
        Card drawn = state.draw();
        assertEquals("W", drawn.toString(),
                "Empty deck with no discard should return fallback wild");
    }

    @Test
    void drawReducesDeckSize() {
        GameState state = freshTwoPlayerState();
        state.buildAndShuffleDeck();
        int before = state.deckSize();
        state.draw();
        assertEquals(before - 1, state.deckSize());
    }

    @Test
    void addToHandIncreasesHandSize() {
        GameState state = freshTwoPlayerState();
        int before = state.handSize(0);
        state.addToHand(0, Card.fromCode("R5"));
        assertEquals(before + 1, state.handSize(0));
    }

    @Test
    void removeFromHandDecreasesHandSize() {
        GameState state = freshTwoPlayerState();
        state.addToHand(0, Card.fromCode("G3"));
        int before = state.handSize(0);
        state.removeFromHand(0, 0);
        assertEquals(before - 1, state.handSize(0));
    }

    @Test
    void getFromHandReturnsCorrectCard() {
        GameState state = freshTwoPlayerState();
        state.addToHand(0, Card.fromCode("Y7"));
        Card last = state.getFromHand(0, state.handSize(0) - 1);
        assertEquals("Y7", last.toString());
    }

    @Test
    void handIsUnmodifiableDirectly() {
        // hand() returns an unmodifiable view — external mutation should throw
        GameState state = freshTwoPlayerState();
        state.addToHand(0, Card.fromCode("R1"));
        assertThrows(UnsupportedOperationException.class,
                () -> state.hand(0).add(Card.fromCode("G2")));
    }

    @Test
    void addScoreAccumulates() {
        GameState state = freshTwoPlayerState();
        state.addScore(0, 30);
        state.addScore(0, 15);
        assertEquals(45, state.getScore(0));
    }

    @Test
    void scoreStartsAtZero() {
        GameState state = freshTwoPlayerState();
        assertEquals(0, state.getScore(0));
        assertEquals(0, state.getScore(1));
    }

    @Test
    void nextAdvancesForwardByDefault() {
        GameState state = freshThreePlayerState();
        state.setCurrentPlayer(0);
        state.setDirection(1);
        state.next();
        assertEquals(1, state.getCurrentPlayer());
    }

    @Test
    void nextWrapsAroundForward() {
        GameState state = freshThreePlayerState();
        state.setCurrentPlayer(2);
        state.setDirection(1);
        state.next();
        assertEquals(0, state.getCurrentPlayer());
    }

    @Test
    void nextWrapsAroundBackward() {
        GameState state = freshThreePlayerState();
        state.setCurrentPlayer(0);
        state.setDirection(-1);
        state.next();
        assertEquals(2, state.getCurrentPlayer());
    }

    @Test
    void skipAdvancesTwoPlayersForward() {
        GameState state = freshThreePlayerState();
        state.setCurrentPlayer(0);
        state.setDirection(1);
        state.next(); // skip victim
        state.next(); // advance to next active player
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
        state.reverseDirection();
        assertEquals(-1, state.getDirection());
    }

    @Test
    void reverseFlipsDirectionNegativeToPositive() {
        GameState state = freshThreePlayerState();
        state.setDirection(-1);
        state.reverseDirection();
        assertEquals(1, state.getDirection());
    }

    @Test
    void reverseInTwoPlayerActsLikeSkip() {
        // Original quirk: 2-player reverse calls next() twice, returning to the same player
        GameState state = freshTwoPlayerState();
        state.setCurrentPlayer(0);
        state.setDirection(1);
        state.reverseDirection(); // direction is now -1
        state.next();             // wraps to player 1
        state.next();             // wraps back to player 0
        assertEquals(0, state.getCurrentPlayer(),
                "2-player reverse + two next() calls should return to original player");
    }

    @Test
    void drawTwoGivesNextPlayerTwoCards() {
        GameState state = freshThreePlayerState();
        state.buildAndShuffleDeck();
        int nextPlayer = 1;
        int before = state.handSize(nextPlayer);
        state.addToHand(nextPlayer, state.draw());
        state.addToHand(nextPlayer, state.draw());
        assertEquals(before + 2, state.handSize(nextPlayer));
    }

    @Test
    void setupPlayersCreatesCorrectCount() {
        GameState state = new GameState(10, new Random(0));
        state.setupPlayers(3, false);
        assertEquals(3, state.playerCount());
    }

    @Test
    void setupPlayersWithHumanAddsHumanFirst() {
        GameState state = new GameState(10, new Random(0));
        state.setupPlayers(2, true);
        assertEquals(3, state.playerCount());
        assertTrue(state.isHuman(0));
        assertFalse(state.isHuman(1));
        assertFalse(state.isHuman(2));
    }

    @Test
    void botPlayersAreNotHuman() {
        GameState state = new GameState(10, new Random(0));
        state.setupPlayers(3, false);
        for (int i = 0; i < 3; i++) {
            assertFalse(state.isHuman(i));
        }
    }

    private GameState freshTwoPlayerState() {
        GameState state = new GameState(10, new Random(0));
        state.setupPlayers(2, false);
        state.setUpCard(Card.fromCode("R9"));
        state.setCalledColor(Card.Color.NONE);
        return state;
    }

    private GameState freshThreePlayerState() {
        GameState state = new GameState(10, new Random(0));
        state.setupPlayers(3, false);
        state.setUpCard(Card.fromCode("R9"));
        state.setCalledColor(Card.Color.NONE);
        return state;
    }
}