package tech.provokedynamic.uno;

import module java.base;
import lombok.extern.slf4j.Slf4j;
import tech.provokedynamic.uno.db.Database;
import tech.provokedynamic.uno.db.SchemaInit;
import tech.provokedynamic.uno.db.StatsReport;
import tech.provokedynamic.uno.db.repository.GameRepository;
import tech.provokedynamic.uno.game.GameRunner;
import tech.provokedynamic.uno.input.ConsoleInput;
import tech.provokedynamic.uno.input.NullInputSource;
import tech.provokedynamic.uno.input.PlayerInputSource;
import tech.provokedynamic.uno.model.GameState;
import tech.provokedynamic.uno.view.ConsoleView;
import tech.provokedynamic.uno.view.GameView;
import tech.provokedynamic.uno.view.SilentView;

/**
 * CLI entry point. Parses arguments, wires up dependencies, and delegates
 * to {@link GameRunner} for the actual match loop.
 * <p>
 * Persistence (H2 + MyBatis) is enabled by default: the schema is applied
 * on startup and the completed match is saved once a target-score winner is
 * found. Use {@code --no-db} to skip persistence, or {@code --report} to
 * print game history and statistics instead of playing.
 */
@Slf4j
class Main {

    static void main(String[] args) {
        CliArgs cli = CliArgs.parse(args);
        if (cli == null) return; // --help was printed

        log.info("Starting UNO: bots={}, target={}, human={}, quiet={}, noDb={}, report={}",
                cli.bots(), cli.target(), cli.human(), cli.quiet(), cli.noDb(), cli.report());

        if (!cli.noDb()) {
            Database.init(Database.h2FileProps(cli.dbPath()));
            SchemaInit.run();
        }

        if (cli.report()) {
            if (cli.noDb()) {
                IO.println("Cannot show --report together with --no-db.");
                return;
            }
            new StatsReport(new GameRepository()).print();
            return;
        }

        GameState state = new GameState(10, new Random(cli.seed()));
        state.setupPlayers(cli.bots(), cli.human());

        if (state.playerCount() < 2 || state.playerCount() > 4) {
            IO.println("UNO needs 2 to 4 players.");
            log.error("Invalid player count: {}", state.playerCount());
            return;
        }

        log.info("Players: {}", IntStream.range(0, state.playerCount())
                .mapToObj(i -> state.playerName(i) + (state.isHuman(i) ? "(human)" : ""))
                .toList());

        GameView view = cli.quiet() ? new SilentView() : new ConsoleView();
        PlayerInputSource input = cli.human()
                ? new ConsoleInput(new Scanner(System.in))
                : new NullInputSource();

        GameRepository repo = cli.noDb() ? null : new GameRepository();

        new GameRunner(state, view, repo, cli.target(), cli.quiet()).run(input);
    }
}
