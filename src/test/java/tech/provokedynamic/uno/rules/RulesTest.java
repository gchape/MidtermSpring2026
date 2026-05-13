package tech.provokedynamic.uno.rules;

import org.junit.jupiter.api.Test;
import tech.provokedynamic.uno.model.Card;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Characterization tests for Rules.isLegal() and Rules.handPoints().
 * <p>
 * Covers every legal-play condition: color match, number match, action-rank
 * match, wild, wild-draw-four, called color, and illegal combinations.
 * No game state or bot logic involved.
 */
class RulesTest {

    @Test
    void matchByColor() {
        assertTrue(Rules.isLegal("R2", "R9", ""));
        assertTrue(Rules.isLegal("GS", "G7", ""));
    }

    @Test
    void differentColorAndNumberIsIllegal() {
        assertFalse(Rules.isLegal("B3", "R9", ""));
    }

    @Test
    void matchByNumber() {
        assertTrue(Rules.isLegal("G9", "R9", ""));
        assertTrue(Rules.isLegal("B0", "Y0", ""));
    }

    @Test
    void differentNumbersSameColorIsLegalByColor() {
        assertTrue(Rules.isLegal("R3", "R7", ""));
    }

    @Test
    void matchBySkipRank() {
        assertTrue(Rules.isLegal("RS", "GS", ""));
    }

    @Test
    void matchByReverseRank() {
        assertTrue(Rules.isLegal("YR", "BR", ""));
    }

    @Test
    void matchByDrawTwoRank() {
        assertTrue(Rules.isLegal("G+2", "R+2", ""));
    }

    @Test
    void skipDoesNotMatchReverse() {
        assertFalse(Rules.isLegal("RS", "YR", ""));
    }

    @Test
    void wildAlwaysLegal() {
        assertTrue(Rules.isLegal("W", "R9", ""));
        assertTrue(Rules.isLegal("W", "GS", ""));
        assertTrue(Rules.isLegal("W", "W4", ""));
    }

    @Test
    void wildDrawFourAlwaysLegal() {
        assertTrue(Rules.isLegal("W4", "B7", ""));
        assertTrue(Rules.isLegal("W4", "W", ""));
    }

    @Test
    void calledColorAllowsPlay() {
        // W was played; blue was called — B3 is now legal
        assertTrue(Rules.isLegal("B3", "W", "B"));
    }

    @Test
    void calledColorDoesNotHelpWrongColor() {
        assertFalse(Rules.isLegal("G3", "W", "B"));
    }

    @Test
    void calledColorOverridesUpCardColor() {
        // Up card is Red, but Blue was called after a wild played on top
        assertTrue(Rules.isLegal("B3", "R9", "B"));
    }

    @Test
    void handPointsSumsAllCards() {
        List<Card> hand = List.of(new Card("R9"), new Card("GS"), new Card("W"));
        assertEquals(9 + 20 + 50, Rules.handPoints(hand));
    }

    @Test
    void emptyHandScoresZero() {
        assertEquals(0, Rules.handPoints(List.of()));
    }
}