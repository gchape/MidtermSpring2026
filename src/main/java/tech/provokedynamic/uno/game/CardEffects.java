package tech.provokedynamic.uno.game;

import tech.provokedynamic.uno.model.Card;

import java.util.EnumMap;
import java.util.Map;

/**
 * Maps each Card.Rank to its post-play effect.
 * Replaces the switch in GameEngine.resolveAction().
 */
public class CardEffects {

    private static final Map<Card.Rank, CardEffect> EFFECTS = new EnumMap<>(Card.Rank.class);

    static {
        EFFECTS.put(Card.Rank.SKIP, (state, _) -> {
            state.next();
            state.next();
        });

        EFFECTS.put(Card.Rank.REVERSE, (state, _) -> {
            state.reverseDirection();
            if (state.playerCount() == 2) {
                state.next();
                state.next();
            } else {
                state.next();
            }
        });

        EFFECTS.put(Card.Rank.DRAW_TWO, (state, view) -> {
            state.next();
            int target = state.getCurrentPlayer();
            state.addToHand(target, state.draw());
            state.addToHand(target, state.draw());
            view.showDrawTwo(state.playerName(target));
            state.next();
        });

        EFFECTS.put(Card.Rank.WILD_DRAW_FOUR, (state, view) -> {
            state.next();
            int target = state.getCurrentPlayer();
            for (int i = 0; i < 4; i++) state.addToHand(target, state.draw());
            view.showDrawFour(state.playerName(target));
            state.next();
        });

        CardEffect advance = (state, _) -> state.next();
        EFFECTS.put(Card.Rank.NUMBER, advance);
        EFFECTS.put(Card.Rank.WILD, advance);
    }

    private CardEffects() {
    }

    public static CardEffect forRank(Card.Rank rank) {
        CardEffect effect = EFFECTS.get(rank);
        if (effect == null) throw new IllegalArgumentException("No effect registered for rank: " + rank);
        return effect;
    }
}