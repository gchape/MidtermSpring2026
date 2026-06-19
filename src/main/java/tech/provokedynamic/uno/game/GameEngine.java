package tech.provokedynamic.uno.game;

import lombok.RequiredArgsConstructor;
import tech.provokedynamic.uno.bot.BotStrategy;
import tech.provokedynamic.uno.input.PlayerInputSource;
import tech.provokedynamic.uno.model.Card;
import tech.provokedynamic.uno.model.GameState;
import tech.provokedynamic.uno.rules.Rules;
import tech.provokedynamic.uno.view.GameView;

import java.util.stream.IntStream;

@RequiredArgsConstructor
public class GameEngine {

    private static final int SAFETY_LIMIT = 3000;
    private static final int INITIAL_HAND_SIZE = 7;

    private final GameState state;
    private final GameView view;

    // Index of a player who is currently exposed to the missed-UNO penalty:
    // they hold one card and have not successfully called UNO. -1 = nobody
    // at risk right now. See resolveUnoCall() and checkMissedUno().
    private int pendingUnoPenalty = -1;

    public void startGame() {
        for (int i = 0; i < state.playerCount(); i++) {
            state.clearHand(i);
        }

        state.buildAndShuffleDeck();

        for (int i = 0; i < state.playerCount(); i++) {
            for (int j = 0; j < INITIAL_HAND_SIZE; j++) {
                state.addToHand(i, state.draw());
            }
        }

        Card first = state.draw();
        while (first.isWild()) {
            state.addToDiscard(first);
            first = state.draw();
        }

        state.setDirection(1);
        state.setUpCard(first);
        state.setCalledColor(Card.Color.NONE);
        state.setCurrentPlayer(state.nextRandomInt(state.playerCount()));
        pendingUnoPenalty = -1;
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

        // Missed-UNO check: if a different player ended last turn on one
        // card without successfully calling UNO, penalize them two cards
        // before this turn proceeds. The window closes once their own turn
        // comes back around (handled inside checkMissedUno()).
        checkMissedUno(cp);

        view.showTurnHeader(
                state.playerName(cp),
                state.getUpCard(),
                state.getCalledColor(),
                state.hand(cp)
        );

        int chosen = handleDraw(cp, chooseCard(cp, input), input);
        return handlePlay(cp, chosen, input);
    }

    private void checkMissedUno(int cp) {
        if (pendingUnoPenalty == -1 || pendingUnoPenalty == cp) {
            pendingUnoPenalty = -1;
            return;
        }

        int suspect = pendingUnoPenalty;
        pendingUnoPenalty = -1;

        if (state.handSize(suspect) == 1) {
            state.addToHand(suspect, state.draw());
            state.addToHand(suspect, state.draw());
            view.showMissedUno(state.playerName(suspect));
        }
    }

    private int chooseCard(int cp, PlayerInputSource input) {
        return state.isHuman(cp)
                ? input.askHuman(state.hand(cp), state.getUpCard(), state.getCalledColor())
                : BotStrategy.chooseCard(state.hand(cp), state.getUpCard(), state.getCalledColor());
    }

    private int handleDraw(int cp, int chosen, PlayerInputSource input) {
        if (chosen != -1) return chosen;

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

    private int handlePlay(int cp, int chosen, PlayerInputSource input) {
        if (chosen < 0) {
            state.next();
            return -1;
        }

        if (chosen >= state.handSize(cp)) {
            view.showIllegalIndex(state.playerName(cp));
            penalise(cp);
            return -1;
        }

        Card card = state.getFromHand(cp, chosen);

        if (!Rules.isLegal(card, state.getUpCard(), state.getCalledColor())) {
            view.showIllegalCard(state.playerName(cp), card);
            penalise(cp);
            return -1;
        }

        playCard(cp, chosen, input);

        if (state.handSize(cp) == 0) {
            int points = computeWinPoints(cp);
            state.addScore(cp, points);
            view.showWin(state.playerName(cp), points);
            return cp;
        }

        CardEffects.forRank(card.rank()).apply(state, view);
        return -1;
    }

    private void playCard(int cp, int chosen, PlayerInputSource input) {
        Card card = state.getFromHand(cp, chosen);
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
            resolveUnoCall(cp, input);
        } else if (pendingUnoPenalty == cp) {
            pendingUnoPenalty = -1;
        }
    }

    /**
     * Called the instant a player's hand drops to one card. Bots always call
     * UNO successfully — a documented simplification (see
     * docs/rules-supported.md) that keeps bot-only games deterministic.
     * Human players are genuinely asked via {@code input.askCallUno()}, and
     * declining (or any non-affirmative answer) leaves them exposed to the
     * missed-UNO penalty on the very next turn.
     */
    private void resolveUnoCall(int cp, PlayerInputSource input) {
        boolean called = state.isHuman(cp) ? input.askCallUno() : BotStrategy.callsUno();

        if (called) {
            view.showUno(state.playerName(cp));
            pendingUnoPenalty = -1;
        } else {
            pendingUnoPenalty = cp;
        }
    }

    private void penalise(int cp) {
        state.addToHand(cp, state.draw());
        state.next();
    }

    private int computeWinPoints(int winner) {
        return IntStream.range(0, state.playerCount())
                .filter(i -> i != winner)
                .map(i -> Rules.handPoints(state.hand(i)))
                .sum();
    }
}
