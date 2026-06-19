package tech.provokedynamic.uno.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * All mutable game state in one place, accessed via getters and setters.
 */
public class GameState {

    private final List<String> playerNames = new ArrayList<>();
    private final List<Boolean> humanPlayers = new ArrayList<>();
    private final List<List<Card>> hands = new ArrayList<>();
    private final List<Card> deck = new ArrayList<>();
    private final List<Card> discard = new ArrayList<>();
    private final int[] scores;

    private final Random random;

    @Setter
    @Getter
    private Card upCard = null;
    @Setter
    @Getter
    private Card.Color calledColor = Card.Color.NONE;
    @Setter
    @Getter
    private int direction = 1;
    @Setter
    @Getter
    private int currentPlayer = 0;

    public GameState(int maxPlayers, Random random) {
        this.scores = new int[maxPlayers];
        this.random = random;
    }

    public void setupPlayers(int bots, boolean human) {
        playerNames.clear();
        humanPlayers.clear();
        hands.clear();

        if (human) {
            playerNames.add("You");
            humanPlayers.add(true);
            hands.add(new ArrayList<>());
        }

        for (int i = 1; i <= bots; i++) {
            playerNames.add("Bot" + i);
            humanPlayers.add(false);
            hands.add(new ArrayList<>());
        }
    }

    public int playerCount() {
        return playerNames.size();
    }

    public String playerName(int i) {
        return playerNames.get(i);
    }

    public boolean isHuman(int i) {
        return humanPlayers.get(i);
    }

    public void reverseDirection() {
        this.direction *= -1;
    }

    public int getScore(int i) {
        return scores[i];
    }

    public int deckSize() {
        return deck.size();
    }

    public int discardSize() {
        return discard.size();
    }

    public List<Card> hand(int index) {
        return Collections.unmodifiableList(hands.get(index));
    }

    public void addToHand(int playerIndex, Card card) {
        hands.get(playerIndex).add(card);
    }

    public void removeFromHand(int playerIndex, int cardIndex) {
        hands.get(playerIndex).remove(cardIndex);
    }

    public int handSize(int playerIndex) {
        return hands.get(playerIndex).size();
    }

    public Card getFromHand(int playerIndex, int cardIndex) {
        return hands.get(playerIndex).get(cardIndex);
    }

    public void addScore(int playerIndex, int points) {
        scores[playerIndex] += points;
    }

    public void buildAndShuffleDeck() {
        deck.clear();

        for (Card.Color color : Card.Color.values()) {
            if (color == Card.Color.NONE) continue;

            deck.add(new Card(color, Card.Rank.NUMBER, 0));

            for (int i = 0; i < 2; i++) {
                for (int n = 1; n <= 9; n++) {
                    deck.add(new Card(color, Card.Rank.NUMBER, n));
                }

                deck.add(new Card(color, Card.Rank.SKIP, -1));
                deck.add(new Card(color, Card.Rank.REVERSE, -1));
                deck.add(new Card(color, Card.Rank.DRAW_TWO, -1));
            }
        }

        for (int i = 0; i < 4; i++) {
            deck.add(new Card(Card.Color.NONE, Card.Rank.WILD, -1));
            deck.add(new Card(Card.Color.NONE, Card.Rank.WILD_DRAW_FOUR, -1));
        }

        Collections.shuffle(deck, random);
    }

    public Card draw() {
        if (deck.isEmpty()) {
            deck.addAll(discard);
            discard.clear();
            Collections.shuffle(deck, random);
        }

        if (deck.isEmpty()) {
            return new Card(Card.Color.NONE, Card.Rank.WILD, -1);
        }

        return deck.removeFirst();
    }

    public void addToDiscard(Card card) {
        discard.add(card);
    }

    public void next() {
        currentPlayer = (currentPlayer + direction + playerNames.size()) % playerNames.size();
    }

    public int nextRandomInt(int bound) {
        return random.nextInt(bound);
    }

    public void clearHand(int playerIndex) {
        hands.get(playerIndex).clear();
    }
}
