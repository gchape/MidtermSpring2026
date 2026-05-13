import tech.provokedynamic.uno.bot.BotStrategy;
import tech.provokedynamic.uno.game.GameEngine;
import tech.provokedynamic.uno.input.ConsoleInput;
import tech.provokedynamic.uno.input.PlayerInputSource;
import tech.provokedynamic.uno.model.Card;
import tech.provokedynamic.uno.model.GameState;
import tech.provokedynamic.uno.rules.Rules;
import tech.provokedynamic.uno.view.ConsoleView;
import tech.provokedynamic.uno.view.GameView;
import tech.provokedynamic.uno.view.SilentView;

static void selfTest() {
    int passed = 0;

    if (Card.fromCode("R5").color() == Card.Color.RED) passed++;
    else fail("color R5");

    if (Card.fromCode("G+2").rank() == Card.Rank.DRAW_TWO) passed++;
    else fail("rank +2");

    if (Card.fromCode("W4").points() == 50) passed++;
    else fail("wild points");

    if (Rules.isLegal(Card.fromCode("R2"), Card.fromCode("R9"), Card.Color.NONE)) passed++;
    else fail("same color");

    if (Rules.isLegal(Card.fromCode("G9"), Card.fromCode("R9"), Card.Color.NONE)) passed++;
    else fail("same number");

    if (Rules.isLegal(Card.fromCode("B3"), Card.fromCode("W"), Card.Color.BLUE)) passed++;
    else fail("called color");

    if (!Rules.isLegal(Card.fromCode("B3"), Card.fromCode("R9"), Card.Color.NONE)) passed++;
    else fail("illegal mismatch");

    var h = new java.util.ArrayList<Card>();
    h.add(Card.fromCode("B3"));
    h.add(Card.fromCode("R4"));
    h.add(Card.fromCode("W"));
    if (BotStrategy.chooseCard(h, Card.fromCode("R9"), Card.Color.NONE) == 1) passed++;
    else fail("bot normal before wild");

    var h2 = new java.util.ArrayList<Card>();
    h2.add(Card.fromCode("B1"));
    h2.add(Card.fromCode("B2"));
    h2.add(Card.fromCode("R3"));
    if (BotStrategy.chooseColor(h2) == Card.Color.BLUE) passed++;
    else fail("bot color");

    IO.println("Passed " + passed + " characterization checks.");
}

static void fail(String name) {
    throw new RuntimeException("Failed: " + name);
}

/**
 * CLI entry point. Parses arguments, wires up GameState + GameEngine,
 * and runs the requested number of games.
 * <p>
 * This class is now thin: it owns no game logic. All game behavior
 * lives in GameEngine, GameState, Rules, BotStrategy, and Card.
 */
void main(String[] args) {
    int bots = 3;
    int games = 1;
    boolean human = false;
    boolean quiet = false;
    long seed = System.currentTimeMillis();

    for (int i = 0; i < args.length; i++) {
        switch (args[i]) {
            case "--bots" -> bots = Integer.parseInt(args[++i]);
            case "--games" -> games = Integer.parseInt(args[++i]);
            case "--human" -> human = true;
            case "--quiet" -> quiet = true;
            case "--seed" -> seed = Long.parseLong(args[++i]);
            case "--self-test" -> {
                selfTest();
                return;
            }
            case "--help" -> {
                IO.println("Usage: scripts/run.sh [--bots N] [--games N] [--human] [--quiet] [--seed N]");
                return;
            }
        }
    }

    GameState state = new GameState(10, new Random(seed));
    state.setupPlayers(bots, human);

    if (state.playerCount() < 2 || state.playerCount() > 4) {
        IO.println("UNO needs 2 to 4 players.");
        return;
    }

    GameView view = quiet ? new SilentView() : new ConsoleView();
    PlayerInputSource inp = human ? new ConsoleInput(new Scanner(System.in)) : null;

    GameEngine engine = new GameEngine(state, view);

    for (int g = 1; g <= games; g++) {
        if (!quiet) IO.println("\n=== Game " + g + " ===");
        engine.playGame(inp);
    }

    IO.println("\nFinal scores:");
    for (int i = 0; i < state.playerCount(); i++) {
        IO.println(state.playerName(i) + ": " + state.getScore(i));
    }
}