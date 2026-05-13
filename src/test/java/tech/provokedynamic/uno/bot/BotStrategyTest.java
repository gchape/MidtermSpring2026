package tech.provokedynamic.uno.bot;

import org.junit.jupiter.api.Test;
import tech.provokedynamic.uno.model.Card;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Characterization tests for BotStrategy.
 * <p>
 * Pins the exact card-priority order and color-selection behavior of the
 * original chooseBotCard() / chooseBotColor(). These are deliberately
 * characterization tests — they assert what the current bot DOES, not
 * what an ideal bot would do.
 * <p>
 * Priority order (draw-two → skip → number → wild → draw) is a quirk of
 * the original implementation and must be preserved unless explicitly changed.
 */
class BotStrategyTest {

    @Test
    void prefersDrawTwoOverSkip() {
        List<Card> hand = List.of(new Card("RS"), new Card("R+2"));
        // Both legal on R9 — draw two (index 1) wins
        assertEquals(1, BotStrategy.chooseCard(hand, new Card("R9"), ""));
    }

    @Test
    void prefersSkipOverNumber() {
        List<Card> hand = List.of(new Card("R7"), new Card("RS"));
        assertEquals(1, BotStrategy.chooseCard(hand, new Card("R9"), ""));
    }

    @Test
    void prefersNumberOverWild() {
        List<Card> hand = List.of(new Card("W"), new Card("R4"));
        // R4 legal on R9; wild is last resort → index 1
        assertEquals(1, BotStrategy.chooseCard(hand, new Card("R9"), ""));
    }

    @Test
    void playsWildWhenNothingElseLegal() {
        List<Card> hand = List.of(new Card("G3"), new Card("W"));
        // G3 illegal on R9; wild at index 1
        assertEquals(1, BotStrategy.chooseCard(hand, new Card("R9"), ""));
    }

    @Test
    void drawsWhenNothingLegal() {
        List<Card> hand = List.of(new Card("G3"), new Card("B7"));
        assertEquals(-1, BotStrategy.chooseCard(hand, new Card("R9"), ""));
    }

    @Test
    void usesCalledColorWhenChoosingCard() {
        // Up card is W, called color is B — B3 should be legal and chosen
        List<Card> hand = List.of(new Card("R5"), new Card("B3"));
        assertEquals(1, BotStrategy.chooseCard(hand, new Card("W"), "B"));
    }

    @Test
    void choosesMostFrequentColor() {
        List<Card> hand = List.of(new Card("B1"), new Card("B2"), new Card("R3"));
        assertEquals("B", BotStrategy.chooseColor(hand));
    }

    @Test
    void tieBreaksRedOverBlue() {
        // R and B tied at 1 each — R wins (tie-break order: R ≥ Y ≥ G ≥ B)
        List<Card> hand = List.of(new Card("R1"), new Card("B2"));
        assertEquals("R", BotStrategy.chooseColor(hand));
    }

    @Test
    void allWildsDefaultsToRed() {
        // No colored cards — all counts zero — falls through to B
        List<Card> hand = List.of(new Card("W"), new Card("W4"));
        assertEquals("R", BotStrategy.chooseColor(hand));
    }
}