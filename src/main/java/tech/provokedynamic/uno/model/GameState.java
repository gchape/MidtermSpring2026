package tech.provokedynamic.uno.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * All mutable game state in one place, accessed via getters and setters.
 * <p>
 * Fields are private. Only state that legitimately needs to change from
 * outside (upCard, calledColor, direction, currentPlayer) has setters.
 * Collections are exposed as unmodifiable views; mutation goes through
 * named methods (draw, addToHand, addScore, etc.) so callers cannot
 * accidentally corrupt internal state.
 */
public class GameState {
    private final List<String> playerNames = new ArrayList<>();
    private final List<Boolean> humanPlayers = new ArrayList<>();
    private final List<List<Card>> hands = new ArrayList<>();
    private final List<Card> deck = new ArrayList<>();
    private final List<Card> discard = new ArrayList<>();
    private final int[] scores;

    private Card upCard = null;
    private String calledColor = "";
    private int direction = 1;
    private int currentPlayer = 0;
    private Random random;

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

    public Card getUpCard() {
        return upCard;
    }

    public void setUpCard(Card upCard) {
        this.upCard = upCard;
    }

    public String getCalledColor() {
        return calledColor;
    }

    public void setCalledColor(String color) {
        this.calledColor = color;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(int p) {
        this.currentPlayer = p;
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

    /**
     * Unmodifiable view — use addToHand / removeFromHand to mutate.
     */
    public List<Card> hand(int index) {
        return Collections.unmodifiableList(hands.get(index));
    }

    public void setRandom(Random random) {
        this.random = random;
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
        String[] colors = {"R", "Y", "G", "B"};
        for (String color : colors) {
            deck.add(new Card(color + "0"));
            for (int n = 1; n <= 9; n++) {
                deck.add(new Card(color + n));
                deck.add(new Card(color + n));
            }
            deck.add(new Card(color + "S"));
            deck.add(new Card(color + "S"));
            deck.add(new Card(color + "R"));
            deck.add(new Card(color + "R"));
            deck.add(new Card(color + "+2"));
            deck.add(new Card(color + "+2"));
        }
        for (int i = 0; i < 4; i++) {
            deck.add(new Card("W"));
            deck.add(new Card("W4"));
        }
        Collections.shuffle(deck, random);
    }

    public Card draw() {
        if (deck.isEmpty()) {
            deck.addAll(discard);
            discard.clear();
            Collections.shuffle(deck, random);
        }
        if (deck.isEmpty()) return new Card("W");
        return deck.removeFirst();
    }

    public void addToDiscard(Card card) {
        discard.add(card);
    }

    public void next() {
        currentPlayer += direction;
        if (currentPlayer >= playerNames.size()) currentPlayer = 0;
        if (currentPlayer < 0) currentPlayer = playerNames.size() - 1;
    }

    public int nextRandomInt(int bound) {
        return random.nextInt(bound);
    }
}