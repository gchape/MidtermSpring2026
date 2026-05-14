import tech.provokedynamic.uno.game.GameEngine;
import tech.provokedynamic.uno.input.ConsoleInput;
import tech.provokedynamic.uno.input.NullInputSource;
import tech.provokedynamic.uno.input.PlayerInputSource;
import tech.provokedynamic.uno.model.GameState;
import tech.provokedynamic.uno.view.ConsoleView;
import tech.provokedynamic.uno.view.GameView;
import tech.provokedynamic.uno.view.SilentView;

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
    PlayerInputSource inp = human ? new ConsoleInput(new Scanner(System.in)) : new NullInputSource();

    GameEngine engine = new GameEngine(state, view);

    for (int g = 1; g <= games; g++) {
        if (!quiet) {
            IO.println("\n=== Game " + g + " ===");
        }

        engine.playGame(inp);
    }

    IO.println("\nFinal scores:");
    for (int i = 0; i < state.playerCount(); i++) {
        IO.println(state.playerName(i) + ": " + state.getScore(i));
    }
}