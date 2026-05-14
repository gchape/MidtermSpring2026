package tech.provokedynamic.uno.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Characterization tests for the Card value object.
 * Pins parsing, color, rank, number, points, isWild, and toString behavior.
 */
class CardTest {

    @Test
    void colorRed() {
        assertEquals(Card.Color.RED, Card.fromCode("R5").color());
    }

    @Test
    void colorYellow() {
        assertEquals(Card.Color.YELLOW, Card.fromCode("Y0").color());
    }

    @Test
    void colorGreen() {
        assertEquals(Card.Color.GREEN, Card.fromCode("GS").color());
    }

    @Test
    void colorBlue() {
        assertEquals(Card.Color.BLUE, Card.fromCode("B+2").color());
    }

    @Test
    void wildHasNoColor() {
        assertEquals(Card.Color.NONE, Card.fromCode("W").color());
        assertEquals(Card.Color.NONE, Card.fromCode("W4").color());
    }

    @Test
    void rankNumber() {
        assertEquals(Card.Rank.NUMBER, Card.fromCode("R5").rank());
    }

    @Test
    void rankSkip() {
        assertEquals(Card.Rank.SKIP, Card.fromCode("GS").rank());
    }

    @Test
    void rankReverse() {
        assertEquals(Card.Rank.REVERSE, Card.fromCode("BR").rank());
    }

    @Test
    void rankDrawTwo() {
        assertEquals(Card.Rank.DRAW_TWO, Card.fromCode("Y+2").rank());
    }

    @Test
    void rankWild() {
        assertEquals(Card.Rank.WILD, Card.fromCode("W").rank());
    }

    @Test
    void rankWildDrawFour() {
        assertEquals(Card.Rank.WILD_DRAW_FOUR, Card.fromCode("W4").rank());
    }

    @Test
    void numberExtracted() {
        assertEquals(7, Card.fromCode("B7").number());
    }

    @Test
    void numberZeroExtracted() {
        assertEquals(0, Card.fromCode("R0").number());
    }

    @Test
    void nonNumberCardsReturnMinusOne() {
        assertEquals(-1, Card.fromCode("RS").number());
        assertEquals(-1, Card.fromCode("BR").number());
        assertEquals(-1, Card.fromCode("G+2").number());
        assertEquals(-1, Card.fromCode("W").number());
        assertEquals(-1, Card.fromCode("W4").number());
    }

    @Test
    void numberCardPointsEqualFaceValue() {
        assertEquals(9, Card.fromCode("R9").points());
        assertEquals(0, Card.fromCode("G0").points());
        assertEquals(5, Card.fromCode("B5").points());
    }

    @Test
    void actionCardPointsTwenty() {
        assertEquals(20, Card.fromCode("YS").points());
        assertEquals(20, Card.fromCode("GR").points());
        assertEquals(20, Card.fromCode("B+2").points());
    }

    @Test
    void wildPointsFifty() {
        assertEquals(50, Card.fromCode("W").points());
        assertEquals(50, Card.fromCode("W4").points());
    }

    @Test
    void isWildTrueForWilds() {
        assertTrue(Card.fromCode("W").isWild());
        assertTrue(Card.fromCode("W4").isWild());
    }

    @Test
    void isWildFalseForColorCards() {
        assertFalse(Card.fromCode("R5").isWild());
        assertFalse(Card.fromCode("GS").isWild());
        assertFalse(Card.fromCode("B+2").isWild());
    }

    @Test
    void toStringRoundTrip() {
        for (String code : new String[]{"R0", "R9", "GS", "BR", "Y+2", "W", "W4", "B3", "Y0"}) {
            assertEquals(code, Card.fromCode(code).toString(),
                    "toString() should reproduce the original code for: " + code);
        }
    }
}