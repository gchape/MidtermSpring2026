package tech.provokedynamic.uno.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeckCompositionTest {

    private List<Card> deck;

    @BeforeEach
    void buildDeck() {
        GameState state = new GameState(4, new Random(0));
        state.buildAndShuffleDeck();
        deck = new ArrayList<>();
        while (state.deckSize() > 0) deck.add(state.draw());
    }

    @Test
    void totalCardCount() {
        assertEquals(108, deck.size());
    }

    @Test
    void fourColorsPresent() {
        for (Card.Color c : List.of(Card.Color.RED, Card.Color.YELLOW, Card.Color.GREEN, Card.Color.BLUE)) {
            assertTrue(deck.stream().anyMatch(card -> card.color() == c), "Missing color " + c);
        }
    }

    @Test
    void oneZeroPerColor() {
        for (Card.Color c : List.of(Card.Color.RED, Card.Color.YELLOW, Card.Color.GREEN, Card.Color.BLUE)) {
            long n = deck.stream()
                    .filter(card -> card.color() == c && card.rank() == Card.Rank.NUMBER && card.number() == 0)
                    .count();
            assertEquals(1, n, "Expected one 0 for " + c);
        }
    }

    @Test
    void twoOfEachNumber1to9PerColor() {
        for (Card.Color c : List.of(Card.Color.RED, Card.Color.YELLOW, Card.Color.GREEN, Card.Color.BLUE)) {
            for (int num = 1; num <= 9; num++) {
                final int n = num;
                long count = deck.stream()
                        .filter(card -> card.color() == c && card.rank() == Card.Rank.NUMBER && card.number() == n)
                        .count();
                assertEquals(2, count, "Expected two " + n + " for " + c);
            }
        }
    }

    @Test
    void twoSkipsPerColor() {
        for (Card.Color c : List.of(Card.Color.RED, Card.Color.YELLOW, Card.Color.GREEN, Card.Color.BLUE)) {
            assertEquals(2, deck.stream().filter(card -> card.color() == c && card.rank() == Card.Rank.SKIP).count());
        }
    }

    @Test
    void twoReversesPerColor() {
        for (Card.Color c : List.of(Card.Color.RED, Card.Color.YELLOW, Card.Color.GREEN, Card.Color.BLUE)) {
            assertEquals(2, deck.stream().filter(card -> card.color() == c && card.rank() == Card.Rank.REVERSE).count());
        }
    }

    @Test
    void twoDrawTwosPerColor() {
        for (Card.Color c : List.of(Card.Color.RED, Card.Color.YELLOW, Card.Color.GREEN, Card.Color.BLUE)) {
            assertEquals(2, deck.stream().filter(card -> card.color() == c && card.rank() == Card.Rank.DRAW_TWO).count());
        }
    }

    @Test
    void fourWilds() {
        assertEquals(4, deck.stream().filter(card -> card.rank() == Card.Rank.WILD).count());
    }

    @Test
    void fourWildDrawFours() {
        assertEquals(4, deck.stream().filter(card -> card.rank() == Card.Rank.WILD_DRAW_FOUR).count());
    }
}
