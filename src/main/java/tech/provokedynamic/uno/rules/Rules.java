package tech.provokedynamic.uno.rules;

import tech.provokedynamic.uno.model.Card;

/**
 * Authoritative home for UNO legal-play rules.
 * <p>
 * Before this extraction, the legality check was copy-pasted in three places:
 * - playGame() inline block
 * - isLegal() static method
 * - chooseBotCard() inline block
 * <p>
 * Now there is exactly one place. All three callers delegate here.
 * This class is stateless and fully testable without running the CLI.
 */
public class Rules {

    private Rules() {
    }

    /**
     * Returns true if {@code card} may legally be played on top of {@code upCard}
     * given the currently called color (NONE if no wild color is active).
     * <p>
     * Legal conditions (any one is sufficient):
     * 1. Card is a wild (always playable).
     * 2. Card color matches up-card color.
     * 3. A color was called after a wild and card matches the called color.
     * 4. Both cards share the same non-number rank (e.g. two skips).
     * 5. Both cards are numbers and share the same digit.
     */
    public static boolean isLegal(Card card, Card upCard, Card.Color calledColor) {
        if (card.isWild()) {
            return true;
        }
        if (card.color() == upCard.color()) {
            return true;
        }
        if (calledColor != Card.Color.NONE && card.color() == calledColor) {
            return true;
        }
        if (card.rank() == upCard.rank() && card.rank() != Card.Rank.NUMBER) {
            return true;
        }
        return card.rank() == Card.Rank.NUMBER
                && upCard.rank() == Card.Rank.NUMBER
                && card.number() == upCard.number();
    }

    /**
     * Returns the point value of a hand (sum of all card point values).
     * Used when computing the winner's score from opponents' remaining cards.
     */
    public static int handPoints(java.util.List<Card> hand) {
        return hand.stream().mapToInt(Card::points).sum();
    }
}