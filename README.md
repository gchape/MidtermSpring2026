# UNO — Final Project

A command-line UNO implementation in Java with full rule support, bot AI, and multi-round scoring.

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

| Flag         | Default | Description                                       |
|--------------|---------|---------------------------------------------------|
| `--bots N`   | 3       | Number of bot players (total players must be 2–4) |
| `--target N` | 500     | Score target to win the match                     |
| `--human`    | off     | Add a human player                                |
| `--quiet`    | off     | Suppress turn-by-turn output                      |
| `--seed N`   | random  | Fix the random seed for reproducible games        |

## Architecture

| Package | Responsibility                                         |
|---------|--------------------------------------------------------|
| `model` | `Card`, `GameState` — pure data, no I/O                |
| `rules` | `Rules` — stateless legality and scoring logic         |
| `game`  | `GameEngine`, `CardEffects`, `CardEffect` — game loop  |
| `bot`   | `BotStrategy` — bot card selection and color calling   |
| `input` | `PlayerInputSource`, `ConsoleInput`, `NullInputSource` |
| `view`  | `GameView`, `ConsoleView`, `SilentView`                |

Game logic is fully testable without a terminal. `GameEngine` only depends on `GameState`, `GameView`, and
`PlayerInputSource` interfaces.

## Running Tests

```sh
mvn test
```

Tests cover deck composition, legal play, all action cards, draw/pass behavior, UNO call and missed-UNO penalty, round
scoring, and multi-round target detection.