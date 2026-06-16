package tech.provokedynamic.uno;

import lombok.extern.slf4j.Slf4j;
import tech.provokedynamic.uno.game.GameEngine;
import tech.provokedynamic.uno.input.ConsoleInput;
import tech.provokedynamic.uno.input.NullInputSource;
import tech.provokedynamic.uno.input.PlayerInputSource;
import tech.provokedynamic.uno.model.GameState;
import tech.provokedynamic.uno.view.ConsoleView;
import tech.provokedynamic.uno.view.GameView;
import tech.provokedynamic.uno.view.SilentView;

import java.util.Random;
import java.util.Scanner;

/**
 * CLI entry point. Parses arguments, wires up GameState + GameEngine,
 * and runs the requested number of games.
 */
@Slf4j
public class Main {

    static void main(String[] args) {
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
                    System.out.println("Usage: java -jar uno.jar [--bots N] [--games N] [--human] [--quiet] [--seed N]");
                    return;
                }
            }
        }

        log.info("Starting UNO: bots={}, games={}, human={}, seed={}", bots, games, human, seed);

        GameState state = new GameState(10, new Random(seed));
        state.setupPlayers(bots, human);

        if (state.playerCount() < 2 || state.playerCount() > 4) {
            System.out.println("UNO needs 2 to 4 players.");
            log.error("Invalid player count: {}", state.playerCount());
            return;
        }

        GameView view = quiet ? new SilentView() : new ConsoleView();
        PlayerInputSource inp = human ? new ConsoleInput(new Scanner(System.in)) : new NullInputSource();

        GameEngine engine = new GameEngine(state, view);

        for (int g = 1; g <= games; g++) {
            if (!quiet) {
                System.out.println("\n=== Game " + g + " ===");
            }
            log.info("=== Game {} of {} starting ===", g, games);
            engine.playGame(inp);
            log.info("=== Game {} finished ===", g);
        }

        System.out.println("\nFinal scores:");
        log.info("Final scores after {} game(s):", games);
        for (int i = 0; i < state.playerCount(); i++) {
            System.out.println(state.playerName(i) + ": " + state.getScore(i));
            log.info("  {}: {}", state.playerName(i), state.getScore(i));
        }
    }
}
