package tech.provokedynamic.uno.game;

import tech.provokedynamic.uno.bot.BotStrategy;
import tech.provokedynamic.uno.input.PlayerInputSource;
import tech.provokedynamic.uno.model.Card;
import tech.provokedynamic.uno.model.GameState;
import tech.provokedynamic.uno.rules.Rules;
import tech.provokedynamic.uno.view.GameView;

import java.util.List;

/**
 * Executes a single game of UNO using a provided GameState.
 * <p>
 * This class contains the turn loop, action resolution, and scoring.
 * It has no console I/O — all output goes through a {@link GameView}.
 * <p>
 * This separation means:
 * - The game loop is testable without a real console.
 * - The view can be swapped (silent for bots-only simulations,
 * rich for a future GUI) without touching game logic.
 * - Rule behavior can be verified in isolation via Rules and Card.
 * <p>
 * Extension point: to add a new card effect, add a case in resolveAction().
 * To add a rule variant, subclass or wrap GameEngine.
 */
public class GameEngine {
    private static final int SAFETY_LIMIT = 3000;

    private final GameState state;
    private final GameView view;

    public GameEngine(GameState state, GameView view) {
        this.state = state;
        this.view = view;
    }

    public void startGame() {
        state.buildAndShuffleDeck();

        for (int i = 0; i < state.playerCount(); i++) {
            for (int j = 0; j < 7; j++) {
                state.addToHand(i, state.draw());
            }
        }

        // First up-card must not be a wild
        Card first = state.draw();
        while (first.isWild()) {
            state.addToDiscard(first);
            first = state.draw();
        }
        state.setUpCard(first);
        state.setCalledColor(Card.Color.NONE); // Enum replacing ""
        state.setDirection(1);
        state.setCurrentPlayer(state.nextRandomInt(state.playerCount()));
    }

    /**
     * Runs one complete game. Returns the index of the winning player,
     * or -1 if the safety limit was reached.
     */
    public int playGame(PlayerInputSource input) {
        startGame();

        for (int guard = 0; guard < SAFETY_LIMIT; guard++) {
            int cp = state.getCurrentPlayer();
            List<Card> hand = state.hand(cp);

            view.showTurnHeader(state.playerName(cp), state.getUpCard(), state.getCalledColor(), hand);

            int chosen = state.isHuman(cp)
                    ? input.askHuman(hand, state.getUpCard(), state.getCalledColor())
                    : BotStrategy.chooseCard(hand, state.getUpCard(), state.getCalledColor());

            // Draw phase
            if (chosen == -1) {
                Card drawn = state.draw();
                state.addToHand(cp, drawn);
                view.showDraw(state.playerName(cp), drawn);
                if (Rules.isLegal(drawn, state.getUpCard(), state.getCalledColor())) {
                    if (state.isHuman(cp)) {
                        chosen = input.askPlayDrawn(drawn) ? state.handSize(cp) - 1 : -1;
                    } else {
                        chosen = state.handSize(cp) - 1;
                    }
                }
            }

            // Play phase
            if (chosen >= 0) {
                if (chosen >= state.handSize(cp)) {
                    view.showIllegalIndex(state.playerName(cp));
                    state.addToHand(cp, state.draw());
                    state.next();
                    continue;
                }

                Card card = state.getFromHand(cp, chosen);

                if (!Rules.isLegal(card, state.getUpCard(), state.getCalledColor())) {
                    view.showIllegalCard(state.playerName(cp), card);
                    state.addToHand(cp, state.draw());
                    state.next();
                    continue;
                }

                state.removeFromHand(cp, chosen);
                state.addToDiscard(state.getUpCard());
                state.setUpCard(card);
                state.setCalledColor(Card.Color.NONE); // Enum replacing ""
                view.showPlay(state.playerName(cp), card);

                // Wild color call
                if (card.isWild()) {
                    Card.Color color = state.isHuman(cp)
                            ? input.askColor()
                            : BotStrategy.chooseColor(state.hand(cp));
                    state.setCalledColor(color);
                    view.showColorCall(state.playerName(cp), color);
                }

                if (state.handSize(cp) == 1) view.showUno(state.playerName(cp));

                // Win check
                if (state.handSize(cp) == 0) {
                    int points = computeWinPoints(cp);
                    state.addScore(cp, points);
                    view.showWin(state.playerName(cp), points);
                    return cp;
                }

                resolveAction(card);

            } else {
                state.next();
            }
        }

        view.showSafetyLimit();
        return -1;
    }

    /**
     * Applies the effect of the card just played (skip, reverse, draw-two, wild-draw-four).
     * Number cards and plain wilds have no additional effect beyond advancing the turn.
     * <p>
     * Extension point: add new card effects here as new cases.
     */
    private void resolveAction(Card card) {
        switch (card.rank()) {
            case SKIP -> {
                state.next();
                state.next();
            }
            case REVERSE -> {
                state.reverseDirection(); // Replaced state.setDirection(state.getDirection() * -1);
                if (state.playerCount() == 2) {
                    state.next();
                    state.next();
                } else {
                    state.next();
                }
            }
            case DRAW_TWO -> {
                state.next();
                int dt = state.getCurrentPlayer();
                state.addToHand(dt, state.draw());
                state.addToHand(dt, state.draw());
                view.showDrawTwo(state.playerName(dt));
                state.next();
            }
            case WILD_DRAW_FOUR -> {
                state.next();
                int wf = state.getCurrentPlayer();
                for (int i = 0; i < 4; i++) state.addToHand(wf, state.draw());
                view.showDrawFour(state.playerName(wf));
                state.next();
            }
            default -> state.next();
        }
    }

    private int computeWinPoints(int winner) {
        int total = 0;
        for (int i = 0; i < state.playerCount(); i++) {
            if (i != winner) total += Rules.handPoints(state.hand(i));
        }
        return total;
    }
}