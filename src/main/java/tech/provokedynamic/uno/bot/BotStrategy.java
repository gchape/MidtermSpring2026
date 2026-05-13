package tech.provokedynamic.uno.bot;

import tech.provokedynamic.uno.model.Card;
import tech.provokedynamic.uno.rules.Rules;

import java.util.List;

/**
 * Bot player decision-making, separated from the game loop.
 * <p>
 * Before this extraction, bot logic lived inside the game loop in Main.java
 * and in chooseBotCard() / chooseBotColor(), both of which duplicated the
 * legality check inline instead of calling isLegal().
 * <p>
 * Now strategy is a clean seam: swap this class to get a smarter bot
 * without touching GameState or the game loop.
 * <p>
 * Priority order (preserves original behavior exactly):
 * 1. Draw two (if legal)
 * 2. Skip (if legal)
 * 3. Any number card (if legal)
 * 4. Wild (last resort)
 * 5. Draw (-1)
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
    public static int chooseCard(List<Card> hand, Card upCard, String calledColor) {
        // Priority 1: draw two
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (card.rank().equals(Card.DRAW_TWO) && Rules.isLegal(card, upCard, calledColor)) {
                return i;
            }
        }
        // Priority 2: skip
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (card.rank().equals(Card.SKIP) && Rules.isLegal(card, upCard, calledColor)) {
                return i;
            }
        }
        // Priority 3: number
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (card.rank().equals(Card.NUMBER) && Rules.isLegal(card, upCard, calledColor)) {
                return i;
            }
        }
        // Priority 4: any wild
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).isWild()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the color the bot calls after playing a wild.
     * Picks whichever color appears most often in the remaining hand.
     * Ties broken in R > Y > G > B order (preserves original behavior).
     */
    public static String chooseColor(List<Card> hand) {
        int r = 0, y = 0, g = 0, b = 0;
        for (Card card : hand) {
            switch (card.color()) {
                case "R" -> r++;
                case "Y" -> y++;
                case "G" -> g++;
                case "B" -> b++;
            }
        }
        if (r >= y && r >= g && r >= b) return "R";
        if (y >= r && y >= g && y >= b) return "Y";
        if (g >= r && g >= y && g >= b) return "G";
        return "B";
    }
}