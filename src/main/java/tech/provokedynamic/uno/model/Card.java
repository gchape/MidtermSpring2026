package tech.provokedynamic.uno.model;

import org.jetbrains.annotations.NotNull;

/**
 * Value object representing a single UNO card.
 * <p>
 * String representations (like "R+2") are generated dynamically
 * rather than stored, ensuring the Enums are the single source of truth.
 */
public record Card(Color color, Rank rank, int number) {

    public static Card fromCode(String code) {
        if (code.equals("W")) return new Card(Color.NONE, Rank.WILD, -1);
        if (code.equals("W4")) return new Card(Color.NONE, Rank.WILD_DRAW_FOUR, -1);

        Color color = switch (code.charAt(0)) {
            case 'R' -> Color.RED;
            case 'Y' -> Color.YELLOW;
            case 'G' -> Color.GREEN;
            case 'B' -> Color.BLUE;
            default -> Color.NONE;
        };

        String value = code.substring(1);
        return switch (value) {
            case "S" -> new Card(color, Rank.SKIP, -1);
            case "R" -> new Card(color, Rank.REVERSE, -1);
            case "+2" -> new Card(color, Rank.DRAW_TWO, -1);
            default -> new Card(color, Rank.NUMBER, Integer.parseInt(value));
        };
    }

    public int points() {
        return rank == Rank.NUMBER ? number : rank.getBaseValue();
    }

    public boolean isWild() {
        return rank == Rank.WILD || rank == Rank.WILD_DRAW_FOUR;
    }

    @Override
    @NotNull
    public String toString() {
        if (rank == Rank.WILD) return "W";
        if (rank == Rank.WILD_DRAW_FOUR) return "W4";

        String colorChar = switch (color) {
            case RED -> "R";
            case YELLOW -> "Y";
            case GREEN -> "G";
            case BLUE -> "B";
            default -> "";
        };

        String rankStr = switch (rank) {
            case NUMBER -> String.valueOf(number);
            case SKIP -> "S";
            case REVERSE -> "R";
            case DRAW_TWO -> "+2";
            default -> "";
        };

        return colorChar + rankStr;
    }

    public enum Color {
        RED, YELLOW, GREEN, BLUE, NONE
    }

    public enum Rank {
        NUMBER(0),
        SKIP(20),
        REVERSE(20),
        DRAW_TWO(20),
        WILD(50),
        WILD_DRAW_FOUR(50);

        private final int baseValue;

        Rank(int baseValue) {
            this.baseValue = baseValue;
        }

        public int getBaseValue() {
            return baseValue;
        }
    }
}