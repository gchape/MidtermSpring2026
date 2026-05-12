# Midterm Exam: Refactor A Working UNO CLI

## Context

You are given a working CLI UNO-like game in `src/Main.java`.

The code is written in a procedural, feature-grown style:

* one large class
* global mutable state
* primitive string card values
* duplicated rule checks
* console input/output mixed with game logic
* turn flow, scoring, parsing, and rule execution mixed together

Your job is not to rewrite it. Your job is to make it safer and easier to change through characterization tests and
incremental refactoring.

## Main Goal

Refactor the existing game toward a clearer design while preserving behavior.

The strongest solutions will move toward an MVC-like separation:

* game state and rules can be tested without the console
* console rendering and prompts are separated from rule execution
* command parsing and turn orchestration are not tangled with card rules

Do not create classes named `Model`, `View`, or `Controller` just to satisfy the phrase MVC. Naming is not evidence of
design. Responsibilities and tests are.

## Required Work

### 1. Understand And Run The Game

From the repository root, enter the midterm project:

```bash
cd midterm-uno-cli
```

Then run the current game:

```bash
scripts/run.sh --human --bots 2 --games 1
```

Run the current checks from the same directory:

```bash
scripts/test.sh
```

Read the implemented rules:

* `docs/rules.html`

### 2. Add Characterization Tests

Before refactoring risky behavior, add tests that describe what the current system does.

Characterization tests are tests for existing behavior. Their purpose is to document how the current code behaves before
you change its design. They are not tests for an ideal version of UNO.

If the current implementation has a quirk, your tests should capture that quirk. For example:

* if a card is legal because its number matches the top card, test that current rule
* if a human can type `draw` even while holding a legal card, test that current behavior
* if illegal input causes a penalty card and turn loss, test that current behavior

These tests protect the game while you extract methods, move responsibilities, and rename code. Refactoring is
successful only if the characterized behavior still works afterward, unless a behavior change is explicitly documented
in your report and covered by tests.

Your tests should cover at least these behaviors:

* matching by color
* matching by number
* matching by action type
* wild and wild draw four behavior
* skip
* reverse
* draw two
* drawing from the deck
* scoring
* at least one edge case that surprised you

Tests do not need to describe perfect UNO. They must describe this implementation.

### 3. Refactor Incrementally

Refactor in small steps. After each meaningful step, the checks should still pass.

Required refactoring outcomes:

* Extract card/rule behavior out of the main turn loop.
* Reduce duplicated legal-play logic.
* Separate at least part of console input/output from game rule logic.
* Make at least one rule behavior testable without running the full CLI game.
* Improve names so responsibilities are easier to understand.

### 4. Preserve Existing Behavior

Unless explicitly documented in your report and covered by tests, the playable behavior should remain the same.

This includes quirks documented in `docs/rules.html`, such as:

* all hands being visible in the terminal
* humans being allowed to type `draw` even when they have a legal play
* illegal index input causing a penalty card and turn loss
* bot players automatically playing drawn cards when legal

### 5. Prepare The Design For One Extension

Your design should make at least one of these possible extensions easier:

* add a rule variant
* add a new card effect
* add a smarter bot strategy
* add a replay log
* replace or improve the CLI view

You do not need to implement the extension during the midterm. You must leave a design that makes such a change
plausible.

## Constraints

Do not:

* rewrite the whole project from scratch
* replace the CLI game with a different game
* introduce a large framework
* hide behavior changes inside refactoring commits
* apply design patterns mechanically
* delete behavior simply because it is awkward

You may:

* add small classes
* add tests or test helpers
* rename methods and variables
* extract methods and classes
* introduce value objects
* introduce MVC-like boundaries
* document known limitations

## Deliverables

Submit:

* refactored source code
* characterization tests
* a short refactoring report in `docs/refactoring-report.md`
* a short extension-readiness note in `docs/extension-readiness.md`

The refactoring report should answer:

* What behavior did you characterize before refactoring?
* What were the worst design problems you found?
* Which refactorings did you perform?
* What behavior did you intentionally preserve?
* What risks remain?

The extension-readiness note should answer:

* Which extension would your design support best?
* Where would that change be implemented?
* What part of your design still makes change difficult?

## Suggested Workflow

1. Run the game manually.
2. Read the rules.
3. Run existing checks.
4. Add characterization tests around one behavior.
5. Refactor one small area.
6. Rerun checks.
7. Repeat.
8. Write the report.

## Evaluation

See `docs/rubric.md`.
