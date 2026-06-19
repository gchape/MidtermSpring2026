# Assignment 4 — UNO CLI

A command-line UNO card game implemented in Java with Maven, SLF4J/Logback logging, and Docker support.

## Requirements

- Java 25+
- Maven 3.9+
- Docker (optional)

---

## Local Workflow

### Build (compile)

```bash
mvn compile
```

### Run tests

```bash
mvn test
```

### Package (create runnable JAR)

```bash
mvn package
```

This produces `target/uno.jar` — a self-contained fat JAR with all dependencies bundled.

### Run locally (without packaging)

```bash
mvn exec:java
```

With options:

```bash
mvn exec:java -Dexec.args="--bots 2 --games 5 --quiet"
mvn exec:java -Dexec.args="--seed 12345 --games 10 --quiet"
```

### Run the packaged JAR

```bash
java -jar target/uno.jar
```

With options:

```bash
java -jar target/uno.jar --bots 3 --games 5 --quiet
java -jar target/uno.jar --bots 2 --human
java -jar target/uno.jar --seed 12345 --games 10 --quiet
```

| Flag        | Default   | Description                       |
|-------------|-----------|-----------------------------------|
| `--bots N`  | `3`       | Number of bot players (1–3)       |
| `--games N` | `1`       | Number of games to play           |
| `--human`   | off       | Add a human player                |
| `--quiet`   | off       | Suppress game output              |
| `--seed N`  | timestamp | RNG seed (for reproducible games) |

---

## Docker Workflow

### Build the Docker image

```bash
docker build -t uno .
```

### Run with Docker (default: 3 bots, 1 game)

```bash
docker run --rm uno
```

### Run with custom options

```bash
docker run --rm uno --bots 2 --games 5 --quiet
docker run --rm uno --seed 99 --games 3
```

### Interactive human game via Docker

```bash
docker run --rm -it uno --bots 2 --human
```

### Persist logs from Docker

```bash
docker run --rm -v "$(pwd)/logs:/app/logs" uno --games 10 --quiet
```

---

## Logging

Game events are logged to `logs/uno.log` (DEBUG level and above). Logged events include:

- Game start (players, first up-card, starting player)
- Each player's turn (player name, up-card, hand size)
- Cards drawn and played
- Bot card and color decisions
- Invalid input / illegal card attempts
- Wild color calls and UNO announcements
- Round end / winner and points scored
- Final scores

Logs rotate daily; the last 7 days are kept. Only `WARN` and above appear on stderr — normal player-facing CLI output is
unaffected.

---

## Project Layout

```
src/
├── main/
│   ├── java/tech/provokedynamic/uno/
│   │   ├── Main.java                  # CLI entry point
│   │   ├── bot/BotStrategy.java       # Bot decision logic
│   │   ├── game/
│   │   │   ├── CardEffect.java        # Effect interface
│   │   │   ├── CardEffects.java       # Effect registry
│   │   │   └── GameEngine.java        # Game loop
│   │   ├── input/
│   │   │   ├── ConsoleInput.java      # Human input via stdin
│   │   │   ├── NullInputSource.java   # No-op for bot-only games
│   │   │   └── PlayerInputSource.java # Input interface
│   │   ├── model/
│   │   │   ├── Card.java              # Card value object
│   │   │   └── GameState.java         # All mutable game state
│   │   ├── rules/Rules.java           # Legal-play rules
│   │   └── view/
│   │       ├── ConsoleView.java       # Console output
│   │       ├── GameView.java          # View interface
│   │       └── SilentView.java        # No-op for tests/quiet mode
│   └── resources/logback.xml          # Logging configuration
└── test/
    └── java/tech/provokedynamic/uno/
        ├── bot/BotStrategyTest.java
        ├── game/GameEngineTest.java
        ├── model/
        │   ├── CardTest.java
        │   └── GameStateTest.java
        └── rules/RulesTest.java
```