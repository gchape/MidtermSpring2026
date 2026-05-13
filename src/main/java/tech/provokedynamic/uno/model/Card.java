package tech.provokedynamic.uno.model;

/**
 * Value object representing a single UNO card.
 * <p>
 * Replaces raw strings like "R+2", "W4", "GS" with a typed object
 * that knows its own color and rank. All parsing is centralized here.
 * <p>
 * Color is empty string for wild cards.
 */
public record Card(String code) {
    public static final String WILD = "WILD";
    public static final String WILD_DRAW_FOUR = "WILD_DRAW_FOUR";
    public static final String SKIP = "SKIP";
    public static final String REVERSE = "REVERSE";
    public static final String DRAW_TWO = "DRAW_TWO";
    public static final String NUMBER = "NUMBER";

    public String color() {
        if (code.startsWith("R")) return "R";
        if (code.startsWith("Y")) return "Y";
        if (code.startsWith("G")) return "G";
        if (code.startsWith("B")) return "B";
        return "";
    }

    public String rank() {
        return switch (code) {
            case "W" -> WILD;
            case "W4" -> WILD_DRAW_FOUR;
            default -> {
                if (code.endsWith("S")) yield SKIP;
                if (code.endsWith("R")) yield REVERSE;
                if (code.endsWith("+2")) yield DRAW_TWO;
                yield NUMBER;
            }
        };
    }

    public int number() {
        if (!rank().equals(NUMBER)) return -1;
        return Integer.parseInt(code.substring(1));
    }

    public boolean isWild() {
        return code.startsWith("W");
    }

    public int points() {
        return switch (rank()) {
            case NUMBER -> number();
            case SKIP, REVERSE, DRAW_TWO -> 20;
            case WILD, WILD_DRAW_FOUR -> 50;
            default -> 0;
        };
    }

    @Override
    public String toString() {
        return code;
    }
}