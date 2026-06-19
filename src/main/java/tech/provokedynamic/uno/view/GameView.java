package tech.provokedynamic.uno.view;

import tech.provokedynamic.uno.model.Card;

import java.util.List;

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

    void showMissedUno(String playerName);

    void showWin(String playerName, int points);

    void showSafetyLimit();
}
