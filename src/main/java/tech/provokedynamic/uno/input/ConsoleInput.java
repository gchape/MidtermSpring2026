package tech.provokedynamic.uno.input;

import tech.provokedynamic.uno.model.Card;
import tech.provokedynamic.uno.rules.Rules;

import java.util.List;
import java.util.Scanner;

/**
 * Human input via the console (Scanner), extracted from Main.
 * Preserves original prompt text and quirks exactly.
 */
public class ConsoleInput implements PlayerInputSource {

    private final Scanner scanner;

    public ConsoleInput(Scanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public int askHuman(List<Card> hand, Card upCard, String calledColor) {
        while (true) {
            IO.print("Choose card index/code or draw: ");

            String input = scanner.nextLine().trim().toUpperCase();

            if (input.equals("DRAW")) return -1;

            try {
                int index = Integer.parseInt(input);
                if (index >= 0 && index < hand.size()) return index;
            } catch (Exception ignored) {
            }

            for (int i = 0; i < hand.size(); i++) {
                if (hand.get(i).code().equals(input)) {
                    if (Rules.isLegal(hand.get(i), upCard, calledColor)) return i;

                    IO.println("That card is not legal.");
                }
            }

            IO.println("Card not found.");
        }
    }

    @Override
    public boolean askPlayDrawn(Card drawn) {
        IO.print("Play drawn card " + drawn + "? y/n: ");

        String answer = scanner.nextLine();

        return answer.equalsIgnoreCase("y") ||
                answer.equalsIgnoreCase("yes");
    }

    @Override
    public String askColor() {
        while (true) {
            IO.print("Call color R/Y/G/B: ");

            String input = scanner.nextLine().trim().toUpperCase();

            switch (input) {
                case "R" -> {
                    return "R";
                }
                case "Y" -> {
                    return "Y";
                }
                case "G" -> {
                    return "G";
                }
                case "B" -> {
                    return "B";
                }
            }
            IO.println("Bad color.");
        }
    }
}