package tech.provokedynamic.uno.game;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import tech.provokedynamic.uno.model.Card;

import java.util.EnumMap;
import java.util.Map;

/**
 * Registry mapping each {@link Card.Rank} to its post-play {@link CardEffect}.
 * <p>
 * Effects mutate {@code GameState} and notify the view. Adding a new card
 * type means registering a new effect here — {@link GameEngine} does not need
 * to change.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CardEffects {

    private static final Map<Card.Rank, CardEffect> EFFECTS = new EnumMap<>(Card.Rank.class);

    static {
        // Skip: advance twice, skipping the next player entirely
        EFFECTS.put(Card.Rank.SKIP, (state, view) -> {
            state.next();
            state.next();
        });

        // Reverse: flip direction; in a two-player game this acts like Skip
        EFFECTS.put(Card.Rank.REVERSE, (state, view) -> {
            state.reverseDirection();
            state.next();
            if (state.playerCount() == 2) state.next();
        });

        // Draw Two: next player draws two cards and loses their turn
        EFFECTS.put(Card.Rank.DRAW_TWO, (state, view) -> {
            state.next();
            int target = state.getCurrentPlayer();
            state.addToHand(target, state.draw());
            state.addToHand(target, state.draw());
            view.showDrawTwo(state.playerName(target));
            state.next();
        });

        // Wild Draw Four: next player draws four cards and loses their turn
        // (color was already chosen in GameEngine.commitPlay before this fires)
        EFFECTS.put(Card.Rank.WILD_DRAW_FOUR, (state, view) -> {
            state.next();
            int target = state.getCurrentPlayer();
            for (int i = 0; i < 4; i++) state.addToHand(target, state.draw());
            view.showDrawFour(state.playerName(target));
            state.next();
        });

        // Number and Wild: just advance to the next player
        CardEffect advance = (state, view) -> state.next();
        EFFECTS.put(Card.Rank.NUMBER, advance);
        EFFECTS.put(Card.Rank.WILD, advance);
    }

    /**
     * Returns the effect registered for {@code rank}.
     *
     * @throws IllegalArgumentException if no effect is registered
     */
    public static CardEffect forRank(Card.Rank rank) {
        CardEffect effect = EFFECTS.get(rank);
        if (effect == null) throw new IllegalArgumentException("No effect registered for rank: " + rank);
        return effect;
    }
}
