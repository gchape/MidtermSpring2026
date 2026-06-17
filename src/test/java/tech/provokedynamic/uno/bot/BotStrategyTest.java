package tech.provokedynamic.uno.bot;

import org.junit.jupiter.api.Test;
import tech.provokedynamic.uno.model.Card;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Characterization tests for BotStrategy.
 * <p>
 * Pins the exact card-priority order and color-selection behavior of the
 * original chooseBotCard() / chooseBotColor(). These are deliberately
 * characterization tests — they assert what the current bot DOES, not
 * what an ideal bot would do.
 * <p>
 * Priority order (draw-two -> skip -> number -> wild -> draw) is a quirk of
 * the original implementation and must be preserved unless explicitly changed.
 */
class BotStrategyTest {

    @Test
    void prefersDrawTwoOverSkip() {
        List<Card> hand = List.of(Card.fromCode("RS"), Card.fromCode("R+2"));
        // Both legal on R9 — draw two (index 1) wins
        assertEquals(1, BotStrategy.chooseCard(hand, Card.fromCode("R9"), Card.Color.NONE));
    }

    @Test
    void prefersSkipOverNumber() {
        List<Card> hand = List.of(Card.fromCode("R7"), Card.fromCode("RS"));
        assertEquals(1, BotStrategy.chooseCard(hand, Card.fromCode("R9"), Card.Color.NONE));
    }

    @Test
    void prefersNumberOverWild() {
        List<Card> hand = List.of(Card.fromCode("W"), Card.fromCode("R4"));
        // R4 legal on R9; wild is last resort — index 1 chosen
        assertEquals(1, BotStrategy.chooseCard(hand, Card.fromCode("R9"), Card.Color.NONE));
    }

    @Test
    void prefersDrawTwoOverNumber() {
        List<Card> hand = List.of(Card.fromCode("R3"), Card.fromCode("R+2"));
        assertEquals(1, BotStrategy.chooseCard(hand, Card.fromCode("R9"), Card.Color.NONE));
    }

    @Test
    void prefersSkipOverWild() {
        List<Card> hand = List.of(Card.fromCode("W"), Card.fromCode("RS"));
        assertEquals(1, BotStrategy.chooseCard(hand, Card.fromCode("R9"), Card.Color.NONE));
    }

    @Test
    void playsWildWhenNothingElseLegal() {
        List<Card> hand = List.of(Card.fromCode("G3"), Card.fromCode("W"));
        // G3 illegal on R9; wild at index 1
        assertEquals(1, BotStrategy.chooseCard(hand, Card.fromCode("R9"), Card.Color.NONE));
    }

    @Test
    void drawsWhenNothingLegal() {
        List<Card> hand = List.of(Card.fromCode("G3"), Card.fromCode("B7"));
        assertEquals(-1, BotStrategy.chooseCard(hand, Card.fromCode("R9"), Card.Color.NONE));
    }

    @Test
    void drawsFromEmptyHand() {
        // Edge case: empty hand should return -1 (draw), not crash
        assertEquals(-1, BotStrategy.chooseCard(List.of(), Card.fromCode("R9"), Card.Color.NONE));
    }

    @Test
    void usesCalledColorWhenChoosingCard() {
        // Up card is W, called color is BLUE — B3 should be legal and chosen (number before wild)
        List<Card> hand = List.of(Card.fromCode("R5"), Card.fromCode("B3"));
        assertEquals(1, BotStrategy.chooseCard(hand, Card.fromCode("W"), Card.Color.BLUE));
    }

    @Test
    void wildIsLegalRegardlessOfCalledColor() {
        // Wild is always playable even when a color is called
        List<Card> hand = List.of(Card.fromCode("W"));
        assertEquals(0, BotStrategy.chooseCard(hand, Card.fromCode("W"), Card.Color.RED));
    }

    @Test
    void choosesMostFrequentColor() {
        List<Card> hand = List.of(Card.fromCode("B1"), Card.fromCode("B2"), Card.fromCode("R3"));
        assertEquals(Card.Color.BLUE, BotStrategy.chooseColor(hand));
    }

    @Test
    void tieBreaksRedOverYellow() {
        List<Card> hand = List.of(Card.fromCode("R1"), Card.fromCode("Y2"));
        assertEquals(Card.Color.RED, BotStrategy.chooseColor(hand));
    }

    @Test
    void tieBreaksRedOverBlue() {
        // R and B tied at 1 each — RED wins (tie-break order: RED >= YELLOW >= GREEN >= BLUE)
        List<Card> hand = List.of(Card.fromCode("R1"), Card.fromCode("B2"));
        assertEquals(Card.Color.RED, BotStrategy.chooseColor(hand));
    }

    @Test
    void tieBreaksYellowOverGreen() {
        List<Card> hand = List.of(Card.fromCode("Y1"), Card.fromCode("G2"));
        assertEquals(Card.Color.YELLOW, BotStrategy.chooseColor(hand));
    }

    @Test
    void tieBreaksGreenOverBlue() {
        List<Card> hand = List.of(Card.fromCode("G1"), Card.fromCode("B2"));
        assertEquals(Card.Color.GREEN, BotStrategy.chooseColor(hand));
    }

    @Test
    void tieBreaksYellowOverBlue() {
        List<Card> hand = List.of(Card.fromCode("Y1"), Card.fromCode("B2"));
        assertEquals(Card.Color.YELLOW, BotStrategy.chooseColor(hand));
    }

    @Test
    void allWildsDefaultsToRed() {
        // No colored cards — all counts zero — falls through to RED
        List<Card> hand = List.of(Card.fromCode("W"), Card.fromCode("W4"));
        assertEquals(Card.Color.RED, BotStrategy.chooseColor(hand));
    }

    @Test
    void singleColorCard() {
        List<Card> hand = List.of(Card.fromCode("G5"));
        assertEquals(Card.Color.GREEN, BotStrategy.chooseColor(hand));
    }

    @Test
    void botsAlwaysCallUno() {
        // Documented simplification: bots never miss a UNO call.
        // Only human players can decline via PlayerInputSource.askCallUno().
        assertTrue(BotStrategy.callsUno());
    }
}
