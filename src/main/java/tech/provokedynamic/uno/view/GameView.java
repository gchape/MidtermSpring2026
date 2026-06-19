package tech.provokedynamic.uno.view;

import tech.provokedynamic.uno.model.Card;

import java.util.List;

/**
 * All console output that the game loop needs to produce.
 * <p>
 * GameEngine calls this interface; it never calls IO.println directly.
 * This means:
 * - Tests use SilentView (no output, no assertions on text).
 * - The CLI uses ConsoleView.
 * - A future GUI would implement its own view.
 */
public interface GameView {

    void showTurnHeader(String playerName, Card upCard, Card.Color calledColor, List<Card> hand);

    void showDraw(String playerName, Card drawn);

    void showPlay(String playerName, Card card);

    void showColorCall(String playerName, Card.Color color);

    void showIllegalIndex(String playerName);

    void showIllegalCard(String playerName, Card card);

    void showDrawTwo(String playerName);

    void showDrawFour(String playerName);

    void showUno(String playerName);

    void showWin(String playerName, int points);

    void showSafetyLimit();
}
