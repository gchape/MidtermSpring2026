package tech.provokedynamic.uno.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardTest {

    @Test
    void colorRed() {
        assertEquals("R", new Card("R5").color());
    }

    @Test
    void colorYellow() {
        assertEquals("Y", new Card("Y0").color());
    }

    @Test
    void colorGreen() {
        assertEquals("G", new Card("GS").color());
    }

    @Test
    void colorBlue() {
        assertEquals("B", new Card("B+2").color());
    }

    @Test
    void wildHasNoColor() {
        assertEquals("", new Card("W").color());
        assertEquals("", new Card("W4").color());
    }

    @Test
    void rankNumber() {
        assertEquals(Card.NUMBER, new Card("R5").rank());
    }

    @Test
    void rankSkip() {
        assertEquals(Card.SKIP, new Card("GS").rank());
    }

    @Test
    void rankReverse() {
        assertEquals(Card.REVERSE, new Card("BR").rank());
    }

    @Test
    void rankDrawTwo() {
        assertEquals(Card.DRAW_TWO, new Card("Y+2").rank());
    }

    @Test
    void rankWild() {
        assertEquals(Card.WILD, new Card("W").rank());
    }

    @Test
    void rankWildDrawFour() {
        assertEquals(Card.WILD_DRAW_FOUR, new Card("W4").rank());
    }

    @Test
    void numberExtracted() {
        assertEquals(7, new Card("B7").number());
    }

    @Test
    void nonNumberCardsReturnMinusOne() {
        assertEquals(-1, new Card("RS").number());
        assertEquals(-1, new Card("W").number());
        assertEquals(-1, new Card("W4").number());
    }

    @Test
    void numberCardPointsEqualFaceValue() {
        assertEquals(9, new Card("R9").points());
        assertEquals(0, new Card("G0").points());
    }

    @Test
    void actionCardPointsTwenty() {
        assertEquals(20, new Card("YS").points());
        assertEquals(20, new Card("GR").points());
        assertEquals(20, new Card("B+2").points());
    }

    @Test
    void wildPointsFifty() {
        assertEquals(50, new Card("W").points());
        assertEquals(50, new Card("W4").points());
    }

    @Test
    void isWildTrueForWilds() {
        assertTrue(new Card("W").isWild());
        assertTrue(new Card("W4").isWild());
    }

    @Test
    void isWildFalseForColorCards() {
        assertFalse(new Card("R5").isWild());
        assertFalse(new Card("GS").isWild());
    }
}
