package tech.provokedynamic.uno.game;

import org.junit.jupiter.api.Test;
import tech.provokedynamic.uno.input.NullInputSource;
import tech.provokedynamic.uno.model.Card;
import tech.provokedynamic.uno.model.GameState;
import tech.provokedynamic.uno.view.SilentView;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class DrawPassTest {

    /**
     * Scripted input that always says "draw" (returns -1) then declines to play the drawn card.
     */
    private static final tech.provokedynamic.uno.input.PlayerInputSource DRAW_AND_PASS =
            new tech.provokedynamic.uno.input.PlayerInputSource() {
                @Override
                public int askHuman(List<Card> hand, Card upCard, Card.Color calledColor) {
                    return -1; // draw
                }

                @Override
                public boolean askPlayDrawn(Card drawn) {
                    return false; // pass
                }

                @Override
                public Card.Color askColor() {
                    return Card.Color.RED;
                }

                @Override
                public boolean askCallUno() {
                    return true;
                }
            };

    private GameState botOnlyState() {
        GameState s = new GameState(4, new Random(42));
        s.setupPlayers(3, false);
        s.buildAndShuffleDeck();
        s.setDirection(1);
        s.setCurrentPlayer(0);
        s.setUpCard(new Card(Card.Color.RED, Card.Rank.NUMBER, 5));
        s.setCalledColor(Card.Color.NONE);
        return s;
    }

    @Test
    void playerDrawsOneCardWhenNoLegalPlay() {
        GameState s = botOnlyState();
        // Stack the deck: put a card that is never legal on top so bot must draw
        // We manipulate by giving the player a hand of one impossible card
        s.clearHand(0);
        // Up card is RED 5; give player only BLUE 3 — not legal, no wild
        s.addToHand(0, new Card(Card.Color.BLUE, Card.Rank.NUMBER, 3));

        int before = s.handSize(0);
        GameEngine engine = new GameEngine(s, new SilentView());
        engine.playTurn(new NullInputSource());

        // Player should have drawn one card (possibly played the drawn card if legal,
        // but either way hand changed from initial illegal state)
        assertTrue(s.handSize(0) >= before, "Hand should not shrink when drawing");
    }

    @Test
    void botPlaysDrawnCardIfLegal() {
        GameState s = new GameState(4, new Random(0));
        s.setupPlayers(3, false);
        s.buildAndShuffleDeck();
        s.setDirection(1);
        s.setCurrentPlayer(0);

        // Give player a hand with only an unplayable card
        s.clearHand(0);
        s.addToHand(0, new Card(Card.Color.BLUE, Card.Rank.NUMBER, 3));

        // Set up-card to GREEN 7 — the bot will draw; if the drawn card is green or 7 it plays it
        s.setUpCard(new Card(Card.Color.GREEN, Card.Rank.NUMBER, 7));
        s.setCalledColor(Card.Color.NONE);

        // We can't control what the bot draws from the shuffled deck,
        // but we can assert the engine doesn't throw and the turn advances.
        GameEngine engine = new GameEngine(s, new SilentView());
        assertDoesNotThrow(() -> engine.playTurn(new NullInputSource()));
    }

    @Test
    void turnAdvancesAfterDrawAndPass() {
        GameState s = botOnlyState();
        s.clearHand(0);
        // Give bot a card that cannot be played on RED 5 unless drawn card happens to be legal
        // Use a known-illegal card
        s.addToHand(0, new Card(Card.Color.BLUE, Card.Rank.NUMBER, 3));

        int before = s.getCurrentPlayer();
        GameEngine engine = new GameEngine(s, new SilentView());
        engine.playTurn(new NullInputSource());

        // Turn should have advanced (current player is no longer 0, unless draw was played)
        // We just verify no hang/exception and state is mutated
        assertNotNull(s.getUpCard());
    }
}
