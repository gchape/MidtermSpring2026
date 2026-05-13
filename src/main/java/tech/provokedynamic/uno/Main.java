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

    if (new Card("R5").color().equals("R")) passed++;
    else fail("color R5");
    if (new Card("G+2").rank().equals(Card.DRAW_TWO)) passed++;
    else fail("rank +2");
    if (new Card("W4").points() == 50) passed++;
    else fail("wild points");
    if (Rules.isLegal("R2", "R9", "")) passed++;
    else fail("same color");
    if (Rules.isLegal("G9", "R9", "")) passed++;
    else fail("same number");
    if (Rules.isLegal("B3", "W", "B")) passed++;
    else fail("called color");
    if (!Rules.isLegal("B3", "R9", "")) passed++;
    else fail("illegal mismatch");

    var h = new java.util.ArrayList<Card>();
    h.add(new Card("B3"));
    h.add(new Card("R4"));
    h.add(new Card("W"));
    if (BotStrategy.chooseCard(h, new Card("R9"), "") == 1) passed++;
    else fail("bot normal before wild");

    var h2 = new java.util.ArrayList<Card>();
    h2.add(new Card("B1"));
    h2.add(new Card("B2"));
    h2.add(new Card("R3"));
    if (BotStrategy.chooseColor(h2).equals("B")) passed++;
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