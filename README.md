# UNO — Final Project

A command-line UNO implementation in Java with full rule support, bot AI, multi-round scoring, and game history
persistence.

## Requirements

- Java 21+ (unnamed class `void main` preview feature)
- Maven 3.9+

## Build and Run

```sh
# Compile
./scripts/compile.sh

# Play a bot-only game (default: 3 bots, target score 500)
./scripts/run.sh

# Play with a human player
./scripts/run.sh --human

# Custom options
./scripts/run.sh --bots 2 --target 200 --seed 42

# Run tests
./scripts/test.sh

# Smoke check (deterministic bot game, asserts non-zero scores)
./scripts/smoke.sh
```

## CLI Options

| Flag             | Default      | Description                                       |
|------------------|--------------|---------------------------------------------------|
| `--bots N`       | 3            | Number of bot players (total players must be 2–4) |
| `--target N`     | 500          | Score target to win the match                     |
| `--human`        | off          | Add a human player                                |
| `--quiet`        | off          | Suppress turn-by-turn output                      |
| `--seed N`       | random       | Fix the random seed for reproducible games        |
| `--db-path PATH` | `./data/uno` | Custom database file path                         |
| `--no-db`        | off          | Disable persistence entirely                      |
| `--report`       | off          | Print game history and statistics, then exit      |

## Architecture

| Package | Class(es)                                               | Responsibility                              |
|---------|---------------------------------------------------------|---------------------------------------------|
| `model` | `Card`, `GameState`                                     | Pure data, no I/O                           |
| `rules` | `Rules`                                                 | Stateless legality and scoring logic        |
| `game`  | `GameEngine`, `CardEffects`, `CardEffect`, `GameRunner` | Single-round loop, card effects, match loop |
| `bot`   | `BotStrategy`                                           | Bot card selection and color calling        |
| `input` | `PlayerInputSource`, `ConsoleInput`, `NullInputSource`  | Human input abstraction                     |
| `view`  | `GameView`, `ConsoleView`, `SilentView`                 | Output abstraction                          |
| (root)  | `Main`, `CliArgs`                                       | CLI wiring; argument parsing                |

Game logic is fully testable without a terminal. `GameEngine` and `GameRunner` depend only on `GameState`,
`GameView`, and `PlayerInputSource` interfaces — never on `Scanner` or `IO` directly.

## Persistence

Game results are saved automatically to an embedded H2 database (`data/uno.mv.db`) after each match.

```sh
# View game history, win counts, and high scores
./scripts/run.sh --report

# Play without saving results
./scripts/run.sh --no-db --quiet --seed 42

# Use a custom database path
./scripts/run.sh --db-path /tmp/myuno
```

See [docs/database.md](docs/database.md) for full schema and query documentation.

## Running Tests

```sh
mvn test
```

Tests cover deck composition, legal play, all action cards, draw/pass behavior, UNO call and missed-UNO penalty, round
scoring, multi-round target detection, and persistence.

## Documentation

- [docs/rules-supported.md](docs/rules-supported.md) — which UNO rules are implemented and which variants are used
- [docs/final-report.md](docs/final-report.md) — architecture, test coverage, and known limitations
- [docs/database.md](docs/database.md) — schema, ORM configuration, and persistence flags