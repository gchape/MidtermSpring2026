package tech.provokedynamic.uno.game;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tech.provokedynamic.uno.bot.BotStrategy;
import tech.provokedynamic.uno.input.PlayerInputSource;
import tech.provokedynamic.uno.model.Card;
import tech.provokedynamic.uno.model.GameState;
import tech.provokedynamic.uno.rules.Rules;
import tech.provokedynamic.uno.view.GameView;

import java.util.stream.IntStream;

/**
 * Drives one UNO round: deals cards, runs the turn loop, and returns the
 * index of the player who emptied their hand first (or -1 if the safety
 * limit was hit).
 * <p>
 * {@code GameEngine} is intentionally unaware of multi-round scoring and
 * persistence — those concerns live in {@link GameRunner}.
 */
@Slf4j
@RequiredArgsConstructor
public class GameEngine {

    static final int SAFETY_LIMIT = 3000;
    private static final int INITIAL_HAND_SIZE = 7;

    /**
     * -1 means nobody is at risk right now.
     */
    private static final int NO_PENALTY = -1;

    private final GameState state;
    private final GameView view;

    /**
     * Index of a player exposed to the missed-UNO penalty: they hold one
     * card and did not call UNO. Cleared when their own turn comes back
     * around or when they are penalised.
     */
    private int pendingUnoPenalty = NO_PENALTY;

    // Public API

    /**
     * Deals cards, sets up the first up-card, and resets turn order.
     */
    public void startGame() {
        dealHands();
        placeFirstUpCard();
        state.setDirection(1);
        state.setCalledColor(Card.Color.NONE);
        state.setCurrentPlayer(state.nextRandomInt(state.playerCount()));
        pendingUnoPenalty = NO_PENALTY;
        log.debug("Round started: upCard={}, firstPlayer={}",
                state.getUpCard(), state.playerName(state.getCurrentPlayer()));
    }

    /**
     * Runs a full round.
     *
     * @return index of the round winner, or -1 if the safety limit was hit
     */
    public int playGame(PlayerInputSource input) {
        startGame();

        for (int guard = 0; guard < SAFETY_LIMIT; guard++) {
            int winner = playTurn(input);
            if (winner >= 0) {
                log.info("Round won by {} after {} turns", state.playerName(winner), guard + 1);
                return winner;
            }
        }

        log.warn("Safety limit ({} turns) reached — no winner this round", SAFETY_LIMIT);
        view.showSafetyLimit();
        return -1;
    }

    // Turn execution

    /**
     * Visible to tests for fine-grained turn-level assertions.
     */
    int playTurn(PlayerInputSource input) {
        int cp = state.getCurrentPlayer();
        log.debug("Turn: player={} handSize={} upCard={}",
                state.playerName(cp), state.handSize(cp), state.getUpCard());

        applyMissedUnoPenaltyIfDue(cp);

        view.showTurnHeader(
                state.playerName(cp),
                state.getUpCard(),
                state.getCalledColor(),
                state.hand(cp)
        );

        int chosen = resolveCardChoice(cp, input);
        return executePlay(cp, chosen, input);
    }

    // Missed-UNO penalty

    private void applyMissedUnoPenaltyIfDue(int currentPlayer) {
        if (pendingUnoPenalty == NO_PENALTY || pendingUnoPenalty == currentPlayer) {
            pendingUnoPenalty = NO_PENALTY;
            return;
        }

        int suspect = pendingUnoPenalty;
        pendingUnoPenalty = NO_PENALTY;

        if (state.handSize(suspect) == 1) {
            state.addToHand(suspect, state.draw());
            state.addToHand(suspect, state.draw());
            log.info("Missed-UNO penalty applied to {}", state.playerName(suspect));
            view.showMissedUno(state.playerName(suspect));
        }
    }

    // Card choice

    private int resolveCardChoice(int cp, PlayerInputSource input) {
        int chosen = state.isHuman(cp)
                ? input.askHuman(state.hand(cp), state.getUpCard(), state.getCalledColor())
                : BotStrategy.chooseCard(state.hand(cp), state.getUpCard(), state.getCalledColor());

        return chosen == -1 ? handleForcedDraw(cp, input) : chosen;
    }

    private int handleForcedDraw(int cp, PlayerInputSource input) {
        Card drawn = state.draw();
        state.addToHand(cp, drawn);
        log.debug("Player {} draws {}", state.playerName(cp), drawn);
        view.showDraw(state.playerName(cp), drawn);

        if (!Rules.isLegal(drawn, state.getUpCard(), state.getCalledColor())) {
            log.debug("Drawn card {} is not legal — passing", drawn);
            return -1;
        }

        if (state.isHuman(cp)) {
            return input.askPlayDrawn(drawn) ? state.handSize(cp) - 1 : -1;
        }
        return state.handSize(cp) - 1;
    }

    // Play execution

    private int executePlay(int cp, int chosen, PlayerInputSource input) {
        if (chosen < 0) {
            state.next();
            return -1;
        }

        if (chosen >= state.handSize(cp)) {
            log.warn("Player {} chose illegal index {} (handSize={})",
                    state.playerName(cp), chosen, state.handSize(cp));
            view.showIllegalIndex(state.playerName(cp));
            penalise(cp);
            return -1;
        }

        Card card = state.getFromHand(cp, chosen);

        if (!Rules.isLegal(card, state.getUpCard(), state.getCalledColor())) {
            log.warn("Player {} tried illegal card {} on upCard={}",
                    state.playerName(cp), card, state.getUpCard());
            view.showIllegalCard(state.playerName(cp), card);
            penalise(cp);
            return -1;
        }

        commitPlay(cp, chosen, input);

        if (state.handSize(cp) == 0) {
            int points = scoreWin(cp);
            state.addScore(cp, points);
            log.info("Player {} wins the round, scoring {} points", state.playerName(cp), points);
            view.showWin(state.playerName(cp), points);
            return cp;
        }

        CardEffects.forRank(card.rank()).apply(state, view);
        return -1;
    }

    private void commitPlay(int cp, int chosen, PlayerInputSource input) {
        Card card = state.getFromHand(cp, chosen);
        state.removeFromHand(cp, chosen);
        state.addToDiscard(state.getUpCard());
        state.setUpCard(card);
        state.setCalledColor(Card.Color.NONE);
        log.debug("Player {} plays {}", state.playerName(cp), card);
        view.showPlay(state.playerName(cp), card);

        if (card.isWild()) {
            Card.Color color = state.isHuman(cp)
                    ? input.askColor()
                    : BotStrategy.chooseColor(state.hand(cp));
            state.setCalledColor(color);
            log.debug("Player {} calls color {}", state.playerName(cp), color);
            view.showColorCall(state.playerName(cp), color);
        }

        if (state.handSize(cp) == 1) {
            resolveUnoCall(cp, input);
        } else if (pendingUnoPenalty == cp) {
            pendingUnoPenalty = NO_PENALTY;
        }
    }

    /**
     * Called the instant a player's hand drops to one card.
     * <p>
     * Bots always call UNO successfully — a documented simplification that
     * keeps bot-only games deterministic (see {@code docs/rules-supported.md}).
     * Human players are genuinely prompted; declining leaves them exposed to
     * the two-card penalty on the next player's turn.
     */
    private void resolveUnoCall(int cp, PlayerInputSource input) {
        boolean called = state.isHuman(cp) ? input.askCallUno() : BotStrategy.callsUno();

        if (called) {
            log.debug("Player {} calls UNO", state.playerName(cp));
            view.showUno(state.playerName(cp));
            pendingUnoPenalty = NO_PENALTY;
        } else {
            log.debug("Player {} did not call UNO — exposed to penalty", state.playerName(cp));
            pendingUnoPenalty = cp;
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void dealHands() {
        for (int i = 0; i < state.playerCount(); i++) {
            state.clearHand(i);
        }
        state.buildAndShuffleDeck();
        for (int i = 0; i < state.playerCount(); i++) {
            for (int j = 0; j < INITIAL_HAND_SIZE; j++) {
                state.addToHand(i, state.draw());
            }
        }
        log.debug("Dealt {} cards to {} players", INITIAL_HAND_SIZE, state.playerCount());
    }

    private void placeFirstUpCard() {
        Card first = state.draw();
        while (first.isWild()) {
            state.addToDiscard(first);
            first = state.draw();
        }
        state.setUpCard(first);
    }

    private void penalise(int cp) {
        state.addToHand(cp, state.draw());
        log.debug("Penalty card given to {}", state.playerName(cp));
        state.next();
    }

    private int scoreWin(int winner) {
        return IntStream.range(0, state.playerCount())
                .filter(i -> i != winner)
                .map(i -> Rules.handPoints(state.hand(i)))
                .sum();
    }
}
