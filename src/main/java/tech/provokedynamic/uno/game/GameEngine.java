package tech.provokedynamic.uno.game;

import tech.provokedynamic.uno.bot.BotStrategy;
import tech.provokedynamic.uno.input.PlayerInputSource;
import tech.provokedynamic.uno.model.Card;
import tech.provokedynamic.uno.model.GameState;
import tech.provokedynamic.uno.rules.Rules;
import tech.provokedynamic.uno.view.GameView;

import java.util.stream.IntStream;

public class GameEngine {
    /**
     * Maximum turns before the game is abandoned.
     * 3000 is enough for any realistic game (4 players × 108 cards × ~7 turns each)
     * while still catching infinite loops in tests quickly.
     */
    private static final int SAFETY_LIMIT = 3000;

    /**
     * Standard UNO starting hand size.
     */
    private static final int INITIAL_HAND_SIZE = 7;

    private final GameState state;
    private final GameView view;

    public GameEngine(GameState state, GameView view) {
        this.state = state;
        this.view = view;
    }

    /**
     * Resets state and deals a fresh hand to every player.
     * Called at the start of each game, including subsequent games on the
     * same engine — clears hands left over from the previous game.
     */
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

        // First up-card must not be a wild — redraw until we get a colored card.
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

    /**
     * Runs one complete game. Returns the index of the winning player,
     * or -1 if the safety limit was reached without a winner.
     */
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

    /**
     * Executes one turn for the current player.
     * Returns the winner index if the current player just emptied their hand,
     * or -1 to continue to the next turn.
     */
    int playTurn(PlayerInputSource input) {
        int cp = state.getCurrentPlayer();

        view.showTurnHeader(
                state.playerName(cp),
                state.getUpCard(),
                state.getCalledColor(),
                state.hand(cp)
        );

        // Choose a card to play (-1 means draw), then draw if needed.
        int chosen = handleDraw(cp, chooseCard(cp, input), input);
        return handlePlay(cp, chosen, input);
    }

    /**
     * Asks the current player (human or bot) which card to play.
     * Returns a hand index, or -1 to draw.
     */
    private int chooseCard(int cp, PlayerInputSource input) {
        return state.isHuman(cp)
                ? input.askHuman(state.hand(cp), state.getUpCard(), state.getCalledColor())
                : BotStrategy.chooseCard(state.hand(cp), state.getUpCard(), state.getCalledColor());
    }

    /**
     * If chosen == -1, draws a card and offers it to the player.
     * Returns the index to play the drawn card, or -1 to keep it and pass.
     * If a card was already chosen, returns it unchanged.
     */
    private int handleDraw(int cp, int chosen, PlayerInputSource input) {
        if (chosen != -1) {
            return chosen;
        }

        Card drawn = state.draw();
        state.addToHand(cp, drawn);
        view.showDraw(state.playerName(cp), drawn);

        // If the drawn card isn't legal, the turn ends with no play.
        if (!Rules.isLegal(drawn, state.getUpCard(), state.getCalledColor())) {
            return -1;
        }

        // Human gets to decide; bot always plays a legal drawn card immediately.
        if (state.isHuman(cp)) {
            return input.askPlayDrawn(drawn) ? state.handSize(cp) - 1 : -1;
        }

        return state.handSize(cp) - 1;
    }

    /**
     * Validates and plays the chosen card.
     * Returns the winner index on win, or -1 to continue.
     * Illegal index or illegal card both result in a penalty card and turn loss.
     */
    private int handlePlay(int cp, int chosen, PlayerInputSource input) {
        if (chosen < 0) {
            // Player drew and kept the card (or had nothing to play).
            state.next();
            return -1;
        }

        if (chosen >= state.handSize(cp)) {
            // Out-of-range index from human input — penalty and turn loss.
            view.showIllegalIndex(state.playerName(cp));
            penalise(cp);
            return -1;
        }

        Card card = state.getFromHand(cp, chosen);

        if (!Rules.isLegal(card, state.getUpCard(), state.getCalledColor())) {
            // Human tried to play an illegal card — penalty and turn loss.
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

        // Apply the card's post-play effect (skip, reverse, draw-two, etc.).
        CardEffects.forRank(card.rank()).apply(state, view);
        return -1;
    }

    /**
     * Commits a card play to state: removes it from hand, pushes the old
     * up-card to discard, sets the new up-card, and handles wild color calls
     * and UNO announcements.
     */
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

        // Announce UNO when the player is down to their last card.
        if (state.handSize(cp) == 1) {
            view.showUno(state.playerName(cp));
        }
    }

    /**
     * Adds a penalty card to the player's hand and advances the turn.
     * Called when a player makes an illegal move.
     */
    private void penalise(int cp) {
        state.addToHand(cp, state.draw());
        state.next();
    }

    /**
     * Sums the point value of all opponents' remaining cards.
     * This is the score awarded to the winner.
     */
    private int computeWinPoints(int winner) {
        return IntStream.range(0, state.playerCount())
                .filter(i -> i != winner)
                .map(i -> Rules.handPoints(state.hand(i)))
                .sum();
    }
}