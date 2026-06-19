package tech.provokedynamic.uno.input;

import tech.provokedynamic.uno.model.Card;

import java.util.List;

/**
 * Fails fast if called. Used for bot-only games where no human input
 * should ever be requested. Surfaces bugs immediately instead of NPE.
 */
public class NullInputSource implements PlayerInputSource {

    @Override
    public int askHuman(List<Card> hand, Card upCard, Card.Color c) {
        throw new IllegalStateException("askHuman called in bot-only game");
    }

    @Override
    public boolean askPlayDrawn(Card drawn) {
        throw new IllegalStateException("askPlayDrawn called in bot-only game");
    }

    @Override
    public Card.Color askColor() {
        throw new IllegalStateException("askColor called in bot-only game");
    }
}
