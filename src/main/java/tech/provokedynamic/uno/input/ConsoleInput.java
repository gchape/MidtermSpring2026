package tech.provokedynamic.uno.input;

import lombok.RequiredArgsConstructor;
import tech.provokedynamic.uno.model.Card;
import tech.provokedynamic.uno.rules.Rules;

import java.util.List;
import java.util.OptionalInt;
import java.util.Scanner;
import java.util.stream.IntStream;

/**
 * Human player input sourced from stdin via {@link Scanner}.
 */
@RequiredArgsConstructor
public class ConsoleInput implements PlayerInputSource {

    private final Scanner scanner;

    @Override
    public int askHuman(List<Card> hand, Card upCard, Card.Color calledColor) {
        while (true) {
            IO.print("Choose card index/code or DRAW: ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("DRAW")) return -1;

            OptionalInt index = parseCardSelection(input, hand);
            if (index.isEmpty()) {
                IO.println("Card not found.");
                continue;
            }

            int i = index.getAsInt();
            if (Rules.isLegal(hand.get(i), upCard, calledColor)) return i;

            IO.println("That card is not legal.");
        }
    }

    @Override
    public boolean askPlayDrawn(Card drawn) {
        IO.print("Play drawn card " + drawn + "? y/n: ");
        String answer = scanner.nextLine().trim();
        return isYes(answer);
    }

    @Override
    public Card.Color askColor() {
        while (true) {
            IO.print("Call color R/Y/G/B: ");
            String input = scanner.nextLine().trim();

            if (input.length() == 1) {
                Card.Color color = Card.Color.fromCode(input.charAt(0));
                if (color != Card.Color.NONE) return color;
            }

            IO.println("Bad color. Enter R, Y, G, or B.");
        }
    }

    @Override
    public boolean askCallUno() {
        IO.print("You have one card left! Call UNO? y/n: ");
        String answer = scanner.nextLine().trim();
        return isYes(answer);
    }

    private OptionalInt parseCardSelection(String input, List<Card> hand) {
        OptionalInt byIndex = parseIndex(input, hand.size());
        return byIndex.isPresent() ? byIndex : findCardByCode(input, hand);
    }

    private OptionalInt parseIndex(String input, int handSize) {
        try {
            int index = Integer.parseInt(input);
            return (index >= 0 && index < handSize) ? OptionalInt.of(index) : OptionalInt.empty();
        } catch (NumberFormatException ignored) {
            return OptionalInt.empty();
        }
    }

    private OptionalInt findCardByCode(String input, List<Card> hand) {
        return IntStream.range(0, hand.size())
                .filter(i -> hand.get(i).toString().equalsIgnoreCase(input))
                .findFirst();
    }

    private boolean isYes(String answer) {
        return answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes");
    }
}
