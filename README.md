# Midterm UNO CLI

A CLI UNO-like game, refactored from a single procedural class into a
testable, MVC-like design. Game rules, state, bot strategy, and console
I/O are separated into distinct classes with clear responsibilities.

## Requirements

- Java 25+
- Maven (or the included `mvnw` wrapper)

## Compile

```bash
scripts/compile.sh
```

## Run Bot Games

```bash
scripts/run.sh --bots 3 --games 5 --quiet
```

## Run Interactive Game

```bash
scripts/run.sh --human --bots 2 --games 1
```

Card input examples:

```text
R5   red 5
YS   yellow skip
BR   blue reverse
G+2  green draw two
W    wild
W4   wild draw four
draw draw a card
```

## Run Tests

```bash
scripts/test.sh
```

Tests are JUnit 5 characterization tests covering rule behavior, card
parsing, bot strategy, state mechanics, and game integration.

## Rules

See `docs/rules.html` for the implemented game rules.

## Project Structure

```text
src/main/java/tech/provokedynamic/uno/
  Main.java                  — CLI entry point, argument parsing
  model/
    Card.java                — Card value object (color, rank, points)
    GameState.java           — All mutable game state
  rules/
    Rules.java               — Legal-play rules and scoring
  bot/
    BotStrategy.java         — Bot card selection and color choice
  game/
    GameEngine.java          — Turn loop and win detection
    CardEffect.java          — Post-play effect interface
    CardEffects.java         — Effect registry (skip, reverse, draw-two, etc.)
  view/
    GameView.java            — Output interface
    ConsoleView.java         — Terminal output
    SilentView.java          — No-op view for tests and quiet mode
  input/
    PlayerInputSource.java   — Human input interface
    ConsoleInput.java        — Terminal input
    NullInputSource.java     — Fails fast in bot-only games

docs/
  rules.html                 — Implemented game rules
  refactoring-report.md      — What was changed and why
  extension-readiness.md     — Where the design supports future change
```