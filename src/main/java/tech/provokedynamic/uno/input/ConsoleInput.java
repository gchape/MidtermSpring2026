package tech.provokedynamic.uno.input;

import lombok.RequiredArgsConstructor;
import tech.provokedynamic.uno.model.Card;
import tech.provokedynamic.uno.rules.Rules;

import java.util.List;
import java.util.OptionalInt;
import java.util.Scanner;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public class ConsoleInput implements PlayerInputSource {

    private final Scanner scanner;

    @Override
    public int askHuman(List<Card> hand, Card upCard, Card.Color calledColor) {
        while (true) {
            IO.print("Choose card index/code or draw: ");

            var input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("DRAW")) {
                return -1;
            }

            var cardSelection = parseCardSelection(input, hand);
            if (cardSelection.isEmpty()) {
                IO.println("Card not found.");
                continue;
            }

            int index = cardSelection.getAsInt();
            if (Rules.isLegal(hand.get(index), upCard, calledColor)) {
                return index;
            }

            IO.println("That card is not legal.");
        }
    }

    private OptionalInt parseCardSelection(String input, List<Card> hand) {
        var index = parseIndex(input, hand);

        if (index.isPresent()) {
            return index;
        }

        return findCardByCode(input, hand);
    }

    private OptionalInt parseIndex(String input, List<Card> hand) {
        try {
            int index = Integer.parseInt(input);

            if (index >= 0 && index < hand.size()) {
                return OptionalInt.of(index);
            }
        } catch (NumberFormatException ignored) {
        }

        return OptionalInt.empty();
    }

    private OptionalInt findCardByCode(String input, List<Card> hand) {
        return IntStream.range(0, hand.size())
                .filter(i -> hand.get(i).toString().equalsIgnoreCase(input))
                .findFirst();
    }

    @Override
    public boolean askPlayDrawn(Card drawn) {
        IO.print("Play drawn card " + drawn + "? y/n: ");

        String answer = scanner.nextLine();

        return answer.equalsIgnoreCase("y")
                || answer.equalsIgnoreCase("yes");
    }

    @Override
    public Card.Color askColor() {
        while (true) {
            IO.print("Call color R/Y/G/B: ");

            String input = scanner.nextLine().trim();

            if (input.length() != 1) {
                IO.println("Bad color.");
                continue;
            }

            Card.Color color = Card.Color.fromCode(input.charAt(0));

            if (color != Card.Color.NONE) {
                return color;
            }

            IO.println("Bad color.");
        }
    }

    @Override
    public boolean askCallUno() {
        IO.print("You have one card left! Call UNO? y/n: ");

        String answer = scanner.nextLine();

        return answer.equalsIgnoreCase("y")
                || answer.equalsIgnoreCase("yes");
    }
}
