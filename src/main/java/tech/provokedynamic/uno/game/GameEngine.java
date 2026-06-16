package tech.provokedynamic.uno.game;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tech.provokedynamic.uno.bot.BotStrategy;
import tech.provokedynamic.uno.input.PlayerInputSource;
import tech.provokedynamic.uno.model.Card;
import tech.provokedynamic.uno.model.GameState;
import tech.provokedynamic.uno.rules.Rules;
import tech.provokedynamic.uno.view.GameView;

import java.util.stream.IntStream;

@Slf4j
@RequiredArgsConstructor
public class GameEngine {

    private static final int SAFETY_LIMIT = 3000;
    private static final int INITIAL_HAND_SIZE = 7;

    private final GameState state;
    private final GameView view;

    /**
     * Number of turns played in the most recent game. Reset by startGame().
     */
    @Getter
    private int turnsPlayed;

    public void startGame() {
        turnsPlayed = 0;

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

        log.info("Game started: {} players, first up-card={}, starting player={}",
                state.playerCount(), first, state.playerName(state.getCurrentPlayer()));
    }

    public int playGame(PlayerInputSource input) {
        startGame();

        for (int guard = 0; guard < SAFETY_LIMIT; guard++) {
            int winner = playTurn(input);
            if (winner >= 0) {
                log.info("Game over: winner={}", state.playerName(winner));
                return winner;
            }
        }

        log.warn("Game stopped at safety limit ({} turns)", SAFETY_LIMIT);
        view.showSafetyLimit();
        return -1;
    }

    int playTurn(PlayerInputSource input) {
        int cp = state.getCurrentPlayer();

        log.debug("Turn: player={}, upCard={}, handSize={}",
                state.playerName(cp), state.getUpCard(), state.handSize(cp));

        view.showTurnHeader(
                state.playerName(cp),
                state.getUpCard(),
                state.getCalledColor(),
                state.hand(cp)
        );

        int chosen = handleDraw(cp, chooseCard(cp, input), input);
        int result = handlePlay(cp, chosen, input);
        turnsPlayed++;
        return result;
    }

    private int chooseCard(int cp, PlayerInputSource input) {
        return state.isHuman(cp)
                ? input.askHuman(state.hand(cp), state.getUpCard(), state.getCalledColor())
                : BotStrategy.chooseCard(state.hand(cp), state.getUpCard(), state.getCalledColor());
    }

    private int handleDraw(int cp, int chosen, PlayerInputSource input) {
        if (chosen != -1) {
            return chosen;
        }

        Card drawn = state.draw();
        state.addToHand(cp, drawn);
        log.info("Draw: player={} drew {}", state.playerName(cp), drawn);
        view.showDraw(state.playerName(cp), drawn);

        if (!Rules.isLegal(drawn, state.getUpCard(), state.getCalledColor())) {
            log.debug("Drawn card {} is not playable for {}", drawn, state.playerName(cp));
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
            log.warn("InvalidInput: player={} chose out-of-range index {}", state.playerName(cp), chosen);
            view.showIllegalIndex(state.playerName(cp));
            penalise(cp);
            return -1;
        }

        Card card = state.getFromHand(cp, chosen);

        if (!Rules.isLegal(card, state.getUpCard(), state.getCalledColor())) {
            log.warn("InvalidInput: player={} attempted illegal card {}", state.playerName(cp), card);
            view.showIllegalCard(state.playerName(cp), card);
            penalise(cp);
            return -1;
        }

        log.info("Play: player={} played {}", state.playerName(cp), card);
        playCard(cp, chosen, input);

        if (state.handSize(cp) == 0) {
            int points = computeWinPoints(cp);
            state.addScore(cp, points);
            log.info("RoundEnd: player={} wins round, points={}", state.playerName(cp), points);
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
            log.info("ColorCall: player={} called {}", state.playerName(cp), color);
            view.showColorCall(state.playerName(cp), color);
        }

        if (state.handSize(cp) == 1) {
            log.info("UNO: player={} says UNO!", state.playerName(cp));
            view.showUno(state.playerName(cp));
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
