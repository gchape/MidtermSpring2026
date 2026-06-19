package tech.provokedynamic.uno.rules;

import org.junit.jupiter.api.Test;
import tech.provokedynamic.uno.model.Card;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegalPlayTest {

    private static Card num(Card.Color c, int n) {
        return new Card(c, Card.Rank.NUMBER, n);
    }

    private static Card action(Card.Color c, Card.Rank r) {
        return new Card(c, r, -1);
    }

    private static Card wild(Card.Rank r) {
        return new Card(Card.Color.NONE, r, -1);
    }

    // --- color match ---

    @Test
    void sameColorIsLegal() {
        assertTrue(Rules.isLegal(num(Card.Color.RED, 3), num(Card.Color.RED, 7), Card.Color.NONE));
    }

    @Test
    void differentColorDifferentNumberIsIllegal() {
        assertFalse(Rules.isLegal(num(Card.Color.BLUE, 3), num(Card.Color.RED, 7), Card.Color.NONE));
    }

    // --- number match ---

    @Test
    void sameNumberDifferentColorIsLegal() {
        assertTrue(Rules.isLegal(num(Card.Color.BLUE, 5), num(Card.Color.RED, 5), Card.Color.NONE));
    }

    @Test
    void differentNumberDifferentColorIsIllegal() {
        assertFalse(Rules.isLegal(num(Card.Color.BLUE, 4), num(Card.Color.RED, 7), Card.Color.NONE));
    }

    // --- action type match ---

    @Test
    void sameActionTypeDifferentColorIsLegal() {
        assertTrue(Rules.isLegal(
                action(Card.Color.BLUE, Card.Rank.SKIP),
                action(Card.Color.RED, Card.Rank.SKIP),
                Card.Color.NONE
        ));
    }

    @Test
    void differentActionTypesIsIllegal() {
        assertFalse(Rules.isLegal(
                action(Card.Color.BLUE, Card.Rank.SKIP),
                action(Card.Color.RED, Card.Rank.REVERSE),
                Card.Color.NONE
        ));
    }

    // --- wilds ---

    @Test
    void wildIsAlwaysLegal() {
        assertTrue(Rules.isLegal(wild(Card.Rank.WILD), num(Card.Color.GREEN, 9), Card.Color.NONE));
    }

    @Test
    void wildDrawFourIsAlwaysLegal() {
        assertTrue(Rules.isLegal(wild(Card.Rank.WILD_DRAW_FOUR), num(Card.Color.YELLOW, 1), Card.Color.NONE));
    }

    // --- called color after wild ---

    @Test
    void calledColorMakesCardLegal() {
        // up-card is a wild, called color is GREEN, playing green card is legal
        assertTrue(Rules.isLegal(num(Card.Color.GREEN, 2), wild(Card.Rank.WILD), Card.Color.GREEN));
    }

    @Test
    void wrongCalledColorIsIllegal() {
        assertFalse(Rules.isLegal(num(Card.Color.BLUE, 2), wild(Card.Rank.WILD), Card.Color.GREEN));
    }
}
