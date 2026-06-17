package tech.provokedynamic.uno.game;

import tech.provokedynamic.uno.model.GameState;
import tech.provokedynamic.uno.view.GameView;

/**
 * The effect a card has on game state after it is played.
 * Each Rank maps to one effect. Adding a new card type means
 * adding a new effect — GameEngine does not need to change.
 */
@FunctionalInterface
public interface CardEffect {

    void apply(GameState state, GameView view);
}
