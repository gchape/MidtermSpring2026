package tech.provokedynamic.uno;

import module java.base;
import tech.provokedynamic.uno.db.Database;
import tech.provokedynamic.uno.db.SchemaInit;
import tech.provokedynamic.uno.db.StatsReport;
import tech.provokedynamic.uno.db.repository.GameRepository;
import tech.provokedynamic.uno.game.GameEngine;
import tech.provokedynamic.uno.input.ConsoleInput;
import tech.provokedynamic.uno.input.NullInputSource;
import tech.provokedynamic.uno.input.PlayerInputSource;
import tech.provokedynamic.uno.model.GameState;
import tech.provokedynamic.uno.view.ConsoleView;
import tech.provokedynamic.uno.view.GameView;
import tech.provokedynamic.uno.view.SilentView;

/**
 * CLI entry point. Wires up state, engine, and view, then runs rounds
 * until a player reaches the target score (default 500).
 * <p>
 * Persistence (H2 + MyBatis) is enabled by default: the schema is applied
 * on startup and the completed match is saved once a target-score winner
 * is found. Use --no-db to skip persistence entirely, or --report to print
 * game history and statistics instead of playing.
 */
class Main {

    static void main(String[] args) {
        int bots = 3;
        int target = 500;
        boolean human = false;
        boolean quiet = false;
        boolean noDb = false;
        boolean report = false;
        long seed = System.currentTimeMillis();
        String dbPath = "./data/uno";

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--bots" -> bots = Integer.parseInt(args[++i]);
                case "--target" -> target = Integer.parseInt(args[++i]);
                case "--human" -> human = true;
                case "--quiet" -> quiet = true;
                case "--seed" -> seed = Long.parseLong(args[++i]);
                case "--db-path" -> dbPath = args[++i];
                case "--no-db" -> noDb = true;
                case "--report" -> report = true;
                case "--help" -> {
                    IO.println("Usage: scripts/run.sh [--bots N] [--target N] [--human] [--quiet] " +
                            "[--seed N] [--db-path PATH] [--no-db] [--report]");
                    return;
                }
            }
        }

        if (!noDb) {
            Database.init(Database.h2FileProps(dbPath));
            SchemaInit.run();
        }

        if (report) {
            if (noDb) {
                IO.println("Cannot show --report together with --no-db.");
                return;
            }
            new StatsReport(new GameRepository()).print();
            return;
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

        LocalDateTime startedAt = LocalDateTime.now();
        int round = 0;
        int overallWinner = -1;

        while (overallWinner == -1) {
            round++;
            if (!quiet) IO.println("\n=== Round " + round + " ===");

            engine.playGame(inp);

            if (!quiet) {
                IO.println("\nScores after round " + round + ":");
                for (int i = 0; i < state.playerCount(); i++) {
                    IO.println("  " + state.playerName(i) + ": " + state.getScore(i));
                }
            }

            for (int i = 0; i < state.playerCount(); i++) {
                if (state.getScore(i) >= target) {
                    overallWinner = i;
                    break;
                }
            }
        }

        IO.println("\n" + state.playerName(overallWinner)
                + " wins the game with " + state.getScore(overallWinner) + " points!");

        IO.println("\nFinal scores:");
        List<String> names = new ArrayList<>();
        int[] finalScores = new int[state.playerCount()];
        for (int i = 0; i < state.playerCount(); i++) {
            IO.println(state.playerName(i) + ": " + state.getScore(i));
            names.add(state.playerName(i));
            finalScores[i] = state.getScore(i);
        }

        if (!noDb) {
            new GameRepository().saveGame(names, finalScores, overallWinner, round, startedAt);
        }
    }
}
