package tech.provokedynamic.uno.rules;

import org.junit.jupiter.api.Test;
import tech.provokedynamic.uno.model.Card;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Characterization tests for Rules.isLegal() and Rules.handPoints().
 * <p>
 * Covers every legal-play condition from the original isLegal() implementation:
 * color match, number match, action-rank match, wild, wild-draw-four,
 * called color, and illegal combinations.
 * No game state or bot logic involved — pure rule verification.
 */
class RulesTest {

    @Test
    void matchByColor() {
        assertTrue(Rules.isLegal(Card.fromCode("R2"), Card.fromCode("R9"), Card.Color.NONE));
        assertTrue(Rules.isLegal(Card.fromCode("GS"), Card.fromCode("G7"), Card.Color.NONE));
        assertTrue(Rules.isLegal(Card.fromCode("B+2"), Card.fromCode("B3"), Card.Color.NONE));
    }

    @Test
    void differentColorAndNumberIsIllegal() {
        assertFalse(Rules.isLegal(Card.fromCode("B3"), Card.fromCode("R9"), Card.Color.NONE));
    }

    @Test
    void differentColorAndDifferentRankIsIllegal() {
        assertFalse(Rules.isLegal(Card.fromCode("GS"), Card.fromCode("BR"), Card.Color.NONE));
    }

    @Test
    void matchByNumber() {
        assertTrue(Rules.isLegal(Card.fromCode("G9"), Card.fromCode("R9"), Card.Color.NONE));
        assertTrue(Rules.isLegal(Card.fromCode("B0"), Card.fromCode("Y0"), Card.Color.NONE));
    }

    @Test
    void differentNumbersSameColorIsLegalByColor() {
        assertTrue(Rules.isLegal(Card.fromCode("R3"), Card.fromCode("R7"), Card.Color.NONE));
    }

    @Test
    void sameNumberDifferentColorIsLegal() {
        assertTrue(Rules.isLegal(Card.fromCode("Y5"), Card.fromCode("B5"), Card.Color.NONE));
    }

    @Test
    void differentNumbersDifferentColorsIsIllegal() {
        assertFalse(Rules.isLegal(Card.fromCode("Y3"), Card.fromCode("B7"), Card.Color.NONE));
    }

    @Test
    void matchBySkipRank() {
        assertTrue(Rules.isLegal(Card.fromCode("RS"), Card.fromCode("GS"), Card.Color.NONE));
    }

    @Test
    void matchByReverseRank() {
        assertTrue(Rules.isLegal(Card.fromCode("YR"), Card.fromCode("BR"), Card.Color.NONE));
    }

    @Test
    void matchByDrawTwoRank() {
        assertTrue(Rules.isLegal(Card.fromCode("G+2"), Card.fromCode("R+2"), Card.Color.NONE));
    }

    @Test
    void skipDoesNotMatchReverse() {
        assertFalse(Rules.isLegal(Card.fromCode("RS"), Card.fromCode("YR"), Card.Color.NONE));
    }

    @Test
    void skipDoesNotMatchDrawTwo() {
        assertFalse(Rules.isLegal(Card.fromCode("RS"), Card.fromCode("Y+2"), Card.Color.NONE));
    }

    @Test
    void wildAlwaysLegal() {
        assertTrue(Rules.isLegal(Card.fromCode("W"), Card.fromCode("R9"), Card.Color.NONE));
        assertTrue(Rules.isLegal(Card.fromCode("W"), Card.fromCode("GS"), Card.Color.NONE));
        assertTrue(Rules.isLegal(Card.fromCode("W"), Card.fromCode("W4"), Card.Color.NONE));
    }

    @Test
    void wildDrawFourAlwaysLegal() {
        assertTrue(Rules.isLegal(Card.fromCode("W4"), Card.fromCode("B7"), Card.Color.NONE));
        assertTrue(Rules.isLegal(Card.fromCode("W4"), Card.fromCode("W"), Card.Color.NONE));
        assertTrue(Rules.isLegal(Card.fromCode("W4"), Card.fromCode("R+2"), Card.Color.NONE));
    }

    @Test
    void calledColorAllowsPlay() {
        // W was played; blue was called — B3 is now legal
        assertTrue(Rules.isLegal(Card.fromCode("B3"), Card.fromCode("W"), Card.Color.BLUE));
    }

    @Test
    void calledColorDoesNotHelpWrongColor() {
        assertFalse(Rules.isLegal(Card.fromCode("G3"), Card.fromCode("W"), Card.Color.BLUE));
    }

    @Test
    void calledColorOverridesUpCardColor() {
        // Up card color is Red, but Blue was called after a wild — Blue card is legal
        assertTrue(Rules.isLegal(Card.fromCode("B3"), Card.fromCode("R9"), Card.Color.BLUE));
    }

    @Test
    void calledColorNoneDoesNotMatchArbitrarily() {
        // NONE called color means no active wild color override
        assertFalse(Rules.isLegal(Card.fromCode("G3"), Card.fromCode("R9"), Card.Color.NONE));
    }

    @Test
    void handPointsSumsAllCards() {
        List<Card> hand = List.of(Card.fromCode("R9"), Card.fromCode("GS"), Card.fromCode("W"));
        assertEquals(9 + 20 + 50, Rules.handPoints(hand));
    }

    @Test
    void emptyHandScoresZero() {
        assertEquals(0, Rules.handPoints(List.of()));
    }

    @Test
    void handPointsWithWildDrawFour() {
        List<Card> hand = List.of(Card.fromCode("W4"), Card.fromCode("B+2"), Card.fromCode("Y7"));
        assertEquals(50 + 20 + 7, Rules.handPoints(hand));
    }

    @Test
    void handPointsAllNumbers() {
        List<Card> hand = List.of(Card.fromCode("R1"), Card.fromCode("G2"), Card.fromCode("B3"));
        assertEquals(6, Rules.handPoints(hand));
    }
}