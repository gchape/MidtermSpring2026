package tech.provokedynamic.uno.bot;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tech.provokedynamic.uno.model.Card;
import tech.provokedynamic.uno.rules.Rules;

import java.util.List;
import java.util.function.Predicate;

/**
 * Bot player decision-making, separated from the game loop.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BotStrategy {

    /**
     * Returns the index of the card the bot wants to play,
     * or -1 if the bot should draw.
     * <p>
     * Priority: DRAW_TWO > SKIP > NUMBER > WILD > draw.
     * Characterization tests pin this order — do not change.
     */
    public static int chooseCard(List<Card> hand, Card upCard, Card.Color calledColor) {
        int index = findLegal(hand, upCard, calledColor, c -> c.rank() == Card.Rank.DRAW_TWO);
        if (index != -1) return index;

        index = findLegal(hand, upCard, calledColor, c -> c.rank() == Card.Rank.SKIP);
        if (index != -1) return index;

        index = findLegal(hand, upCard, calledColor, c -> c.rank() == Card.Rank.NUMBER);
        if (index != -1) return index;

        index = findLegal(hand, upCard, calledColor, Card::isWild);
        log.debug("BotChoice: chose index={} (hand={}, upCard={})", index, hand, upCard);
        return index;
    }

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
     * Ties broken in RED > YELLOW > GREEN > BLUE order.
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
                }
            }
        }

        Card.Color chosen;
        if (r >= y && r >= g && r >= b) chosen = Card.Color.RED;
        else if (y >= r && y >= g && y >= b) chosen = Card.Color.YELLOW;
        else if (g >= r && g >= y && g >= b) chosen = Card.Color.GREEN;
        else chosen = Card.Color.BLUE;

        log.debug("BotColor: chose {} (r={}, y={}, g={}, b={})", chosen, r, y, g, b);
        return chosen;
    }
}
