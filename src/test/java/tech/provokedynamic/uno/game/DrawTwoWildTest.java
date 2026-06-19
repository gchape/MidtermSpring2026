package tech.provokedynamic.uno.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.provokedynamic.uno.input.NullInputSource;
import tech.provokedynamic.uno.model.Card;
import tech.provokedynamic.uno.model.GameState;
import tech.provokedynamic.uno.rules.Rules;
import tech.provokedynamic.uno.view.SilentView;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class DrawTwoWildTest {

    private final SilentView view = new SilentView();
    private GameState state;

    @BeforeEach
    void setUp() {
        state = new GameState(4, new Random(0));
        state.setupPlayers(3, false);
        state.buildAndShuffleDeck();
        state.setDirection(1);
        state.setCurrentPlayer(0);
        state.setUpCard(new Card(Card.Color.RED, Card.Rank.NUMBER, 5));
        state.setCalledColor(Card.Color.NONE);
    }

    // --- Draw Two (1.5) ---

    @Test
    void drawTwoGivesNextPlayerTwoCards() {
        int before = state.handSize(1);
        CardEffects.forRank(Card.Rank.DRAW_TWO).apply(state, view);
        assertEquals(before + 2, state.handSize(1));
    }

    @Test
    void drawTwoSkipsNextPlayer() {
        // current = 0 → after draw-two player 1 is skipped → current = 2
        CardEffects.forRank(Card.Rank.DRAW_TWO).apply(state, view);
        assertEquals(2, state.getCurrentPlayer());
    }

    @Test
    void drawTwoDoesNotAffectCurrentPlayer() {
        int before = state.handSize(0);
        CardEffects.forRank(Card.Rank.DRAW_TWO).apply(state, view);
        assertEquals(before, state.handSize(0));
    }

    // --- Wild color (1.6) ---

    @Test
    void calledColorAfterWildAffectsLegality() {
        state.setUpCard(new Card(Card.Color.NONE, Card.Rank.WILD, -1));
        state.setCalledColor(Card.Color.GREEN);

        assertTrue(Rules.isLegal(new Card(Card.Color.GREEN, Card.Rank.NUMBER, 3),
                state.getUpCard(), state.getCalledColor()));

        assertFalse(Rules.isLegal(new Card(Card.Color.BLUE, Card.Rank.NUMBER, 3),
                state.getUpCard(), state.getCalledColor()));
    }

    @Test
    void wildItselfIsAlwaysLegal() {
        assertTrue(Rules.isLegal(
                new Card(Card.Color.NONE, Card.Rank.WILD, -1),
                new Card(Card.Color.RED, Card.Rank.NUMBER, 7),
                Card.Color.NONE
        ));
    }

    /**
     * Closes the gap between the unit-level color tests above (which feed a
     * manually-set called color straight into Rules.isLegal) and the actual
     * engine path: this drives a real bot turn through GameEngine and
     * confirms BotStrategy.chooseColor()'s choice actually lands on
     * GameState via GameEngine.playCard().
     */
    @Test
    void botPlayingWildThroughEngineSetsCalledColorOnState() {
        // Bot 0's hand: one illegal BLUE card plus a Wild — the bot must play the Wild,
        // and BLUE is the only color left in hand afterward, so the color choice is forced.
        state.clearHand(0);
        state.addToHand(0, new Card(Card.Color.BLUE, Card.Rank.NUMBER, 9)); // illegal vs RED 5
        state.addToHand(0, new Card(Card.Color.NONE, Card.Rank.WILD, -1));

        GameEngine engine = new GameEngine(state, view);
        engine.playTurn(new NullInputSource());

        assertEquals(Card.Color.BLUE, state.getCalledColor(),
                "Bot should call the color most represented in its remaining hand after playing Wild");
        assertEquals(Card.Rank.WILD, state.getUpCard().rank());
    }

    // --- Wild Draw Four (1.7) ---

    @Test
    void wildDrawFourGivesNextPlayerFourCards() {
        int before = state.handSize(1);
        CardEffects.forRank(Card.Rank.WILD_DRAW_FOUR).apply(state, view);
        assertEquals(before + 4, state.handSize(1));
    }

    @Test
    void wildDrawFourSkipsNextPlayer() {
        CardEffects.forRank(Card.Rank.WILD_DRAW_FOUR).apply(state, view);
        assertEquals(2, state.getCurrentPlayer());
    }

    @Test
    void wildDrawFourIsAlwaysLegal() {
        Card wdf = new Card(Card.Color.NONE, Card.Rank.WILD_DRAW_FOUR, -1);
        assertTrue(Rules.isLegal(wdf, new Card(Card.Color.BLUE, Card.Rank.NUMBER, 9), Card.Color.NONE));
    }

    /**
     * Full integration: bot plays a forced Wild Draw Four through the real
     * engine, and the color choice, the four-card draw, and the turn skip
     * all land correctly together — not just in isolated unit tests.
     */
    @Test
    void botPlayingWildDrawFourThroughEngineAppliesColorAndDrawFour() {
        state.clearHand(0);
        state.addToHand(0, new Card(Card.Color.GREEN, Card.Rank.NUMBER, 9)); // illegal vs RED 5
        state.addToHand(0, new Card(Card.Color.NONE, Card.Rank.WILD_DRAW_FOUR, -1));

        int targetBefore = state.handSize(1);

        GameEngine engine = new GameEngine(state, view);
        engine.playTurn(new NullInputSource());

        assertEquals(Card.Color.GREEN, state.getCalledColor(),
                "Color choice should reflect the bot's remaining hand");
        assertEquals(targetBefore + 4, state.handSize(1),
                "Next player should draw four cards");
        assertEquals(2, state.getCurrentPlayer(),
                "Turn should skip the targeted player and land on the one after");
    }
}
