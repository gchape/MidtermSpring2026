package tech.provokedynamic.uno.game;

import tech.provokedynamic.uno.bot.BotStrategy;
import tech.provokedynamic.uno.input.PlayerInputSource;
import tech.provokedynamic.uno.model.Card;
import tech.provokedynamic.uno.model.GameState;
import tech.provokedynamic.uno.rules.Rules;
import tech.provokedynamic.uno.view.GameView;

/**
 * Executes a single game of UNO using a provided GameState.
 * All output goes through a {@link GameView} — no console I/O here.
 */
public class GameEngine {
    /**
     * Maximum turns before the game is abandoned.
     * 3000 is enough for any realistic game (4 players × 108 cards × ~7 turns each)
     * while still catching infinite loops in tests quickly.
     */
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

        state.setDirection(1);
        state.setUpCard(first);
        state.setCalledColor(Card.Color.NONE);
        state.setCurrentPlayer(state.nextRandomInt(state.playerCount()));
    }

    public int playGame(PlayerInputSource input) {
        startGame();

        for (int guard = 0; guard < SAFETY_LIMIT; guard++) {
            int winner = playTurn(input);

            if (winner >= 0) {
                return winner;
            }
        }

        view.showSafetyLimit();

        return -1;
    }

    int playTurn(PlayerInputSource input) {
        int cp = state.getCurrentPlayer();

        view.showTurnHeader(
                state.playerName(cp),
                state.getUpCard(),
                state.getCalledColor(),
                state.hand(cp)
        );

        int chosen = handleDraw(
                cp,
                chooseCard(cp, input),
                input
        );

        return handlePlay(cp, chosen, input);
    }

    private int chooseCard(int cp, PlayerInputSource input) {
        return state.isHuman(cp)
                ? input.askHuman(state.hand(cp), state.getUpCard(), state.getCalledColor())
                : BotStrategy.chooseCard(state.hand(cp), state.getUpCard(), state.getCalledColor());
    }

    /**
     * If the player must draw (chosen == -1), draws a card and returns
     * the index to play it, or -1 to keep it.
     */
    private int handleDraw(int cp, int chosen, PlayerInputSource input) {
        if (chosen != -1) {
            return chosen;
        }

        Card drawn = state.draw();

        state.addToHand(cp, drawn);
        view.showDraw(state.playerName(cp), drawn);

        if (!Rules.isLegal(drawn, state.getUpCard(), state.getCalledColor())) {
            return -1;
        }

        if (state.isHuman(cp)) {
            return input.askPlayDrawn(drawn) ? state.handSize(cp) - 1 : -1;
        }

        return state.handSize(cp) - 1;
    }

    /**
     * Attempts to play the chosen card. Returns the winner index, or -1 to continue.
     */
    private int handlePlay(int cp, int chosen, PlayerInputSource input) {
        if (chosen < 0) {
            state.next();
            return -1;
        }

        if (chosen >= state.handSize(cp)) {
            view.showIllegalIndex(state.playerName(cp));
            state.addToHand(cp, state.draw());
            state.next();
            return -1;
        }

        Card card = state.getFromHand(cp, chosen);

        if (!Rules.isLegal(card, state.getUpCard(), state.getCalledColor())) {
            view.showIllegalCard(state.playerName(cp), card);
            state.addToHand(cp, state.draw());
            state.next();
            return -1;
        }

        playCard(cp, chosen, card, input);

        if (state.handSize(cp) == 0) {
            int points = computeWinPoints(cp);

            state.addScore(cp, points);
            view.showWin(state.playerName(cp), points);
            return cp;
        }

        resolveAction(card);
        return -1;
    }

    private void playCard(int cp, int chosen, Card card, PlayerInputSource input) {
        state.removeFromHand(cp, chosen);
        state.addToDiscard(state.getUpCard());
        state.setUpCard(card);
        state.setCalledColor(Card.Color.NONE);
        view.showPlay(state.playerName(cp), card);

        if (card.isWild()) {
            Card.Color color = state.isHuman(cp)
                    ? input.askColor()
                    : BotStrategy.chooseColor(state.hand(cp));
            state.setCalledColor(color);
            view.showColorCall(state.playerName(cp), color);
        }

        if (state.handSize(cp) == 1) {
            view.showUno(state.playerName(cp));
        }
    }

    /**
     * Applies the effect of the card just played via {@link CardEffects}.
     * To add a new card effect, register a new entry in CardEffects — no
     * changes needed here.
     */
    private void resolveAction(Card card) {
        CardEffects.forRank(card.rank()).apply(state, view);
    }

    private int computeWinPoints(int winner) {
        int total = 0;

        for (int i = 0; i < state.playerCount(); i++) {
            if (i != winner) {
                total += Rules.handPoints(state.hand(i));
            }
        }

        return total;
    }
}