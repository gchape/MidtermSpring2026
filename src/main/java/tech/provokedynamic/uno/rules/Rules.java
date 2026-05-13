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
     * given the currently called color (empty string if none).
     * <p>
     * Legal conditions (any one is sufficient):
     * 1. Card is a wild (always playable).
     * 2. Card color matches up-card color.
     * 3. A color was called after a wild and card matches the called color.
     * 4. Both cards share the same non-number rank (e.g. two skips).
     * 5. Both cards are numbers and share the same digit.
     */
    public static boolean isLegal(Card card, Card upCard, String calledColor) {
        if (card.isWild()) {
            return true;
        }
        if (card.color().equals(upCard.color())) {
            return true;
        }
        if (!calledColor.isEmpty() && card.color().equals(calledColor)) {
            return true;
        }
        if (card.rank().equals(upCard.rank()) && !card.rank().equals(Card.NUMBER)) {
            return true;
        }
        return card.rank().equals(Card.NUMBER)
                && upCard.rank().equals(Card.NUMBER)
                && card.number() == upCard.number();
    }

    /**
     * Convenience overload accepting raw string codes, for callers that have
     * not yet been migrated to Card objects.
     */
    public static boolean isLegal(String cardCode, String upCode, String calledColor) {
        return isLegal(new Card(cardCode), new Card(upCode), calledColor);
    }

    /**
     * Returns the point value of a hand (sum of all card point values).
     * Used when computing the winner's score from opponents' remaining cards.
     */
    public static int handPoints(java.util.List<Card> hand) {
        return hand.stream().mapToInt(Card::points).sum();
    }
}