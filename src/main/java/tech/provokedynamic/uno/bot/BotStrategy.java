package tech.provokedynamic.uno.bot;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tech.provokedynamic.uno.model.Card;
import tech.provokedynamic.uno.rules.Rules;

import java.util.List;
import java.util.function.Predicate;

/**
 * Stateless bot decision-making, separated from the game loop.
 * <p>
 * All methods are pure functions: same input always produces the same output.
 * This makes bot behavior deterministic and independently testable.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BotStrategy {

    /**
     * Returns the hand index of the card the bot wants to play, or -1 to draw.
     * <p>
     * Priority order (characterization tests pin this — do not change):
     * <ol>
     *   <li>DRAW_TWO</li>
     *   <li>SKIP</li>
     *   <li>NUMBER</li>
     *   <li>Any Wild (WILD or WILD_DRAW_FOUR)</li>
     *   <li>-1 (draw)</li>
     * </ol>
     */
    public static int chooseCard(List<Card> hand, Card upCard, Card.Color calledColor) {
        int index;

        if ((index = findLegal(hand, upCard, calledColor, c -> c.rank() == Card.Rank.DRAW_TWO)) != -1) return index;
        if ((index = findLegal(hand, upCard, calledColor, c -> c.rank() == Card.Rank.SKIP)) != -1) return index;
        if ((index = findLegal(hand, upCard, calledColor, c -> c.rank() == Card.Rank.NUMBER)) != -1) return index;
        if ((index = findLegal(hand, upCard, calledColor, Card::isWild)) != -1) {
            log.debug("BotChoice: wild at index={} (upCard={})", index, upCard);
            return index;
        }

        return -1;
    }

    /**
     * Returns the color the bot calls after playing a wild.
     * Picks whichever non-NONE color appears most often in the remaining hand;
     * ties broken RED > YELLOW > GREEN > BLUE.
     */
    public static Card.Color chooseColor(List<Card> hand) {
        int r = 0, y = 0, g = 0, b = 0;
        for (Card card : hand) {
            switch (card.color()) {
                case RED -> r++;
                case YELLOW -> y++;
                case GREEN -> g++;
                case BLUE -> b++;
                default -> { /* wild — no color */ }
            }
        }

        Card.Color chosen;
        if (r >= y && r >= g && r >= b) chosen = Card.Color.RED;
        else if (y >= g && y >= b) chosen = Card.Color.YELLOW;
        else if (g >= b) chosen = Card.Color.GREEN;
        else chosen = Card.Color.BLUE;

        log.debug("BotColor: chose {} (r={}, y={}, g={}, b={})", chosen, r, y, g, b);
        return chosen;
    }

    /**
     * Whether the bot calls UNO when its hand drops to one card.
     * <p>
     * Bots always call successfully — a deliberate simplification that keeps
     * bot-only games deterministic. Only human players, via
     * {@code PlayerInputSource.askCallUno()}, can decline and risk the penalty.
     */
    public static boolean callsUno() {
        return true;
    }

    private static int findLegal(List<Card> hand, Card upCard, Card.Color calledColor,
                                 Predicate<Card> condition) {
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (condition.test(card) && Rules.isLegal(card, upCard, calledColor)) return i;
        }
        return -1;
    }
}
