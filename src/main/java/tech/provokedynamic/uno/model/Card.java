package tech.provokedynamic.uno.model;

import org.jetbrains.annotations.NotNull;

/**
 * Value object representing a single UNO card.
 */
public record Card(Color color, Rank rank, int number) {

    /**
     * Create a Card from a code like "R5", "G+2", "W", "W4".
     */
    public static Card fromCode(String code) {
        code = code.trim().toUpperCase();

        // Wild cards
        if (code.equals("W")) return new Card(Color.NONE, Rank.WILD, -1);
        if (code.equals("W4")) return new Card(Color.NONE, Rank.WILD_DRAW_FOUR, -1);

        Color color = Color.fromCode(code.charAt(0));
        String rankCode = code.substring(1);

        return switch (rankCode) {
            case "S" -> new Card(color, Rank.SKIP, -1);
            case "R" -> new Card(color, Rank.REVERSE, -1);
            case "+2" -> new Card(color, Rank.DRAW_TWO, -1);
            default -> new Card(color, Rank.NUMBER, Integer.parseInt(rankCode));
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
        return colorCode() + rankCode();
    }

    private String colorCode() {
        return switch (color) {
            case RED -> "R";
            case YELLOW -> "Y";
            case GREEN -> "G";
            case BLUE -> "B";
            case NONE -> "";
        };
    }

    private String rankCode() {
        return switch (rank) {
            case NUMBER -> String.valueOf(number);
            case SKIP -> "S";
            case REVERSE -> "R";
            case DRAW_TWO -> "+2";
            case WILD, WILD_DRAW_FOUR -> "";
        };
    }

    public enum Color {
        RED, YELLOW, GREEN, BLUE, NONE;

        public static Color fromCode(char code) {
            return switch (Character.toUpperCase(code)) {
                case 'R' -> RED;
                case 'Y' -> YELLOW;
                case 'G' -> GREEN;
                case 'B' -> BLUE;
                default -> NONE;
            };
        }
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