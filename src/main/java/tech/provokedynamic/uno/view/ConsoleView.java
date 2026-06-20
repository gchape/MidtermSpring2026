package tech.provokedynamic.uno.view;

import tech.provokedynamic.uno.model.Card;

import java.util.List;

/**
 * Human-readable console output for UNO game events.
 */
public class ConsoleView implements GameView {

    private static String formatHand(List<Card> cards) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cards.size(); i++) {
            if (i > 0) sb.append(' ');
            sb.append(i).append(':').append(cards.get(i));
        }
        return sb.toString();
    }

    private static String colorChar(Card.Color color) {
        return switch (color) {
            case RED -> "R";
            case YELLOW -> "Y";
            case GREEN -> "G";
            case BLUE -> "B";
            default -> "";
        };
    }

    @Override
    public void showTurnHeader(String playerName, Card upCard, Card.Color calledColor, List<Card> hand) {
        String colorSuffix = calledColor == Card.Color.NONE ? "" : " called " + colorChar(calledColor);
        IO.println("\nUp card: " + upCard + colorSuffix);
        IO.println(playerName + " hand: " + formatHand(hand));
    }

    @Override
    public void showDraw(String playerName, Card drawn) {
        IO.println(playerName + " draws " + drawn);
    }

    @Override
    public void showPlay(String playerName, Card card) {
        IO.println(playerName + " plays " + card);
    }

    @Override
    public void showColorCall(String playerName, Card.Color color) {
        IO.println(playerName + " calls " + colorChar(color));
    }

    @Override
    public void showIllegalIndex(String playerName) {
        IO.println(playerName + " selected an invalid index and draws a penalty card.");
    }

    @Override
    public void showIllegalCard(String playerName, Card card) {
        IO.println(playerName + " tried illegal card " + card + " and draws a penalty card.");
    }

    @Override
    public void showDrawTwo(String playerName) {
        IO.println(playerName + " draws two.");
    }

    @Override
    public void showDrawFour(String playerName) {
        IO.println(playerName + " draws four.");
    }

    @Override
    public void showUno(String playerName) {
        IO.println(playerName + " says UNO!");
    }

    @Override
    public void showMissedUno(String playerName) {
        IO.println(playerName + " forgot to say UNO and draws two cards.");
    }

    @Override
    public void showWin(String playerName, int points) {
        IO.println(playerName + " wins the round and scores " + points + " points.");
    }

    @Override
    public void showSafetyLimit() {
        IO.println("Game stopped at safety limit.");
    }
}
