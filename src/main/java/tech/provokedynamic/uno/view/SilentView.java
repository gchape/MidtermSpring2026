package tech.provokedynamic.uno.view;

import tech.provokedynamic.uno.model.Card;

import java.util.List;

public class SilentView implements GameView {

    @Override
    public void showTurnHeader(String n, Card u, Card.Color c, List<Card> h) {
    }

    @Override
    public void showDraw(String n, Card d) {
    }

    @Override
    public void showPlay(String n, Card c) {
    }

    @Override
    public void showColorCall(String n, Card.Color c) {
    }

    @Override
    public void showIllegalIndex(String n) {
    }

    @Override
    public void showIllegalCard(String n, Card c) {
    }

    @Override
    public void showDrawTwo(String n) {
    }

    @Override
    public void showDrawFour(String n) {
    }

    @Override
    public void showUno(String n) {
    }

    @Override
    public void showMissedUno(String n) {
    }

    @Override
    public void showWin(String n, int p) {
    }

    @Override
    public void showSafetyLimit() {
    }
}
