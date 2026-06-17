package tech.provokedynamic.uno.input;

import tech.provokedynamic.uno.model.Card;

import java.util.List;

/**
 * Input from a human player.
 * <p>
 * GameEngine calls this interface for human turns; it never touches
 * Scanner directly. This allows tests to inject scripted input without
 * needing a real terminal.
 */
public interface PlayerInputSource {
    /**
     * Ask which card to play. Returns index into hand, or -1 to draw.
     */
    int askHuman(List<Card> hand, Card upCard, Card.Color calledColor);

    /**
     * After drawing a card, ask whether to play it.
     */
    boolean askPlayDrawn(Card drawn);

    /**
     * Ask which color to call after playing a wild.
     */
    Card.Color askColor();

    /**
     * Ask whether the player wants to call UNO after dropping to one card.
     * Declining (or any non-affirmative response) leaves the player exposed
     * to the missed-UNO penalty on the next turn. Bots never go through this
     * method — see {@code BotStrategy.callsUno()}.
     */
    boolean askCallUno();
}
