package tech.provokedynamic.uno.view;

import tech.provokedynamic.uno.model.Card;

import java.util.List;

/**
 * Console implementation of GameView.
 * Reproduces the original IO.println output exactly — behavior preserved.
 */
public class ConsoleView implements GameView {

    private static String join(List<Card> cards) {
        var sb = new StringBuilder();
        for (int i = 0; i < cards.size(); i++) {
            sb.append(i).append(":").append(cards.get(i));
            if (i < cards.size() - 1) sb.append(" ");
        }
        return sb.toString();
    }

    @Override
    public void showTurnHeader(String playerName, Card upCard, String calledColor, List<Card> hand) {
        IO.println("\nUp card: " + upCard + (calledColor.isEmpty() ? "" : " called " + calledColor));
        IO.println(playerName + " hand: " + join(hand));
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
    public void showColorCall(String playerName, String color) {
        IO.println(playerName + " calls " + color);
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
    public void showWin(String playerName, int points) {
        IO.println(playerName + " wins and scores " + points);
    }

    @Override
    public void showSafetyLimit() {
        IO.println("Game stopped at safety limit.");
    }
}