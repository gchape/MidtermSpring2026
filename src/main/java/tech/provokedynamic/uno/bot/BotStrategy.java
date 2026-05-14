package tech.provokedynamic.uno.bot;

import tech.provokedynamic.uno.model.Card;
import tech.provokedynamic.uno.rules.Rules;

import java.util.List;
import java.util.function.Predicate;

/**
 * Bot player decision-making, separated from the game loop.
 */
public class BotStrategy {

    private BotStrategy() {
    }

    /**
     * Returns the index of the card the bot wants to play,
     * or -1 if the bot should draw.
     * <p>
     * The priority order is intentionally preserved from the original
     * chooseBotCard() — characterization tests pin this behavior.
     */
    public static int chooseCard(List<Card> hand, Card upCard, Card.Color calledColor) {
        int index = findLegal(hand, upCard, calledColor, c -> c.rank() == Card.Rank.DRAW_TWO);
        if (index != -1) return index;

        index = findLegal(hand, upCard, calledColor, c -> c.rank() == Card.Rank.SKIP);
        if (index != -1) return index;

        index = findLegal(hand, upCard, calledColor, c -> c.rank() == Card.Rank.NUMBER);
        if (index != -1) return index;

        return findLegal(hand, upCard, calledColor, Card::isWild);
    }

    /**
     * Helper method to find the first legal card matching a specific condition.
     */
    private static int findLegal(List<Card> hand, Card upCard, Card.Color calledColor, Predicate<Card> condition) {
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);

            if (condition.test(card) && Rules.isLegal(card, upCard, calledColor)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Returns the color the bot calls after playing a wild.
     * Picks whichever color appears most often in the remaining hand.
     * Ties broken in RED > YELLOW > GREEN > BLUE order (preserves original behavior).
     */
    public static Card.Color chooseColor(List<Card> hand) {
        int r = 0, y = 0, g = 0, b = 0;
        for (Card card : hand) {
            switch (card.color()) {
                case RED -> r++;
                case YELLOW -> y++;
                case GREEN -> g++;
                case BLUE -> b++;
                default -> {
                } // NONE (Wild cards) don't count towards the highest color
            }
        }

        if (r >= y && r >= g && r >= b) return Card.Color.RED;
        if (y >= r && y >= g && y >= b) return Card.Color.YELLOW;
        if (g >= r && g >= y && g >= b) return Card.Color.GREEN;
        return Card.Color.BLUE;
    }
}