# Refactoring report

## What behavior was characterized before refactoring

Characterization tests were written before any production code was changed,
covering the behaviors most at risk during structural changes:

**Rule behaviors (RulesTest)**

- Match by color (same color, different rank)
- Match by number (same digit, different color)
- Match by action rank (skip-on-skip, reverse-on-reverse, draw-two-on-draw-two)
- Wild and wild-draw-four are always legal regardless of up-card
- Called color allows a card of that color to be played
- Called color NONE means no override is active
- Illegal combinations: different color, different number, mismatched action ranks

**Card representation (CardTest)**

- Color, rank, number, points, isWild — all parsed correctly from codes
- toString() round-trips back to the original code string
- Non-number cards return -1 for number()

**Bot behavior (BotStrategyTest)**

- Priority order: draw-two before skip, skip before number, number before wild,
  draw when nothing is legal — this order is a quirk of the original
  chooseBotCard() and the tests pin it exactly
- Bot color selection: most-frequent color wins
- Tie-break order: RED >= YELLOW >= GREEN >= BLUE (original quirk preserved)
- All-wilds hand defaults to RED

**State mechanics (GameStateTest)**

- Draw from empty deck reshuffles the discard pile
- Draw from completely empty deck (no discard either) returns fallback wild
- Hand add/remove/get operations
- hand() returns an unmodifiable view (external mutation throws)
- Scoring accumulates correctly across multiple calls
- next() advances forward and wraps correctly
- next() with direction -1 advances backward and wraps
- Two next() calls skip a player (characterizes skip/draw-two effect)
- reverseDirection() flips sign
- Two-player reverse + two next() calls returns to the original player (quirk)

**Game integration (GameEngineTest)**

- Bot-only game produces a valid winner (not -1)
- Winner score is positive
- Scores accumulate across multiple games
- Two-player and four-player games complete without hitting the safety limit
- Fixed seed produces the same winner on every run (deterministic characterization)
- Illegal index causes a penalty card and turn advance (documented quirk)
- Illegal card causes a penalty card and turn advance (documented quirk)
- Bots autoplay drawn cards when legal — verified by running 10 games without stalls

## Worst design problems found

**1. Duplicated legality check.**
`isLegal()` existed as a static method but `chooseBotCard()` and the
inline check inside `playGame()` each re-implemented the same logic
independently. The three copies could drift apart without any compile-time
or test failure. The bot's inline copy had a subtle structural difference
(it evaluated card.startsWith("W") before checking called color) that
happened to produce the same results but was not obviously equivalent.

**2. Primitive card strings.**
Every card was a raw String. `color()`, `rank()`, `number()`, and `points()`
were free functions in Main that parsed strings on every call. There was no
type safety: passing "R+2" where a color was expected would compile silently.
Adding a new card type would require updating every switch in every function.

**3. Console I/O tangled into the game loop.**
The ~200-line `playGame()` method called `System.out.println` directly
throughout. Running a simulated game (e.g. for a bot tournament or a test)
required capturing stdout, and there was no way to verify turn outcomes
by inspecting state rather than output text.

**4. Global mutable static state.**
All state (`deck`, `hands`, `scores`, `upCard`, `calledColor`, `direction`,
`currentPlayer`) lived in `static` fields on Main. Any test that exercised
one part of the loop contaminated state for every subsequent test. Multiple
concurrent games were structurally impossible.

**5. Bot strategy mixed with rule knowledge.**
`chooseBotCard()` re-implemented the legality check inline. A change to the
legal-play rules would need to be applied in three places: `isLegal()`,
the inline check in `playGame()`, and the inline check in `chooseBotCard()`.

**6. Scoring mixed with win detection.**
Point calculation happened inline at the moment of win detection inside the
game loop. There was no way to test scoring logic independently.

## Refactoring steps performed

Each step was followed by running all characterization tests before proceeding.

### Step 1 — Write characterization tests

Added tests for `isLegal`, `color`, `rank`, `number`, `points`, bot priority
order, and bot color selection. No production code changed in this step.

### Step 2 — Extract Card value object

Replaced raw string cards with a `Card` record owning `color()`, `rank()`,
`number()`, `points()`, `isWild()`, `fromCode()`, and `toString()`.
All parsing is now in one place. The original free functions were deleted.
Tests confirmed the round-trip invariant: `Card.fromCode(s).toString()` == s
for every code in the test suite.

### Step 3 — Extract Rules.isLegal()

Created `Rules` as the single authoritative home for the legality check.
All three callers — the inline play check, the bot card selector, and the
draw-then-play check — now delegate here. The duplicate copies were removed.
`Rules.handPoints()` was also extracted here so scoring logic could be tested
independently.

### Step 4 — Extract GameState

Bundled all global static fields into a `GameState` object with getters and
setters. Tests construct `GameState` directly, configure it, and verify
behavior without touching any CLI code. The `hand()` accessor returns an
unmodifiable list so external code cannot corrupt hands without going through
the proper mutation methods.

### Step 5 — Extract BotStrategy

Moved `chooseBotCard()` and `chooseBotColor()` into a stateless `BotStrategy`
class. The implementation now delegates to `Rules.isLegal()` — the inline
duplication is gone. Priority order and tie-break behavior are preserved
exactly as characterized by the tests.

### Step 6 — Separate console I/O with GameView and PlayerInputSource

Introduced the `GameView` interface with two implementations:

- `ConsoleView` reproduces the original `IO.println` output exactly.
- `SilentView` is a no-op, replacing the original `quiet` flag.

Introduced the `PlayerInputSource` interface, `ConsoleInput` implementation,
and `NullInputSource` which fails fast if called in a bot-only game — replacing
the implicit `null` contract with an explicit error.
The game loop in `GameEngine` calls only these interfaces — no `IO.println`,
no `Scanner` directly.

### Step 7 — Extract GameEngine

Moved the turn loop, action resolution, and score computation out of Main
into `GameEngine`. Main is now a thin argument parser that constructs the
wired-together objects and calls `engine.playGame()`.

### Step 8 — Replace resolveAction switch with CardEffect registry

Introduced `CardEffect` (a `@FunctionalInterface`) and `CardEffects` (an
`EnumMap` registry mapping each `Card.Rank` to its post-play effect).
`GameEngine.resolveAction()` became a one-liner delegating to
`CardEffects.forRank()`. Adding a new card type now requires registering
one entry in `CardEffects` — `GameEngine` does not need to change.

### Step 9 — Extract playTurn for single-turn testability

Extracted the loop body from `playGame()` into a package-visible
`playTurn(PlayerInputSource)` method returning the winner index or -1.
`playGame()` became a simple loop over `playTurn()`. This made the
penalty-card quirk tests testable by driving the engine directly rather
than manually replicating its state transitions.

### Step 10 — Extract chooseCard / handleDraw / handlePlay / playCard

Decomposed `playTurn()` into four private methods, each with one job:

- `chooseCard()` — delegates to human input or bot strategy
- `handleDraw()` — draws a card if needed, returns whether to play it
- `handlePlay()` — validates and plays the chosen card, detects win
- `playCard()` — commits the card to state, handles wild color call and UNO announcement

`playTurn()` now reads as a three-step pipeline: choose, draw if needed, play.

## Behavior intentionally preserved

The following behaviors are preserved exactly, including quirks:

- All hands are visible in the terminal during human play (ConsoleView
  calls showTurnHeader with the full hand list before each turn).
- Humans may type `draw` even when holding a legal card. ConsoleInput.askHuman()
  returns -1 any time the input is "DRAW", without checking legality first.
- An out-of-range index causes a penalty card and turn loss. The engine
  checks `chosen >= handSize` before attempting to play the card, adds a
  penalty, calls `state.next()`, and continues.
- Bot players automatically play a drawn card when it is legal. After drawing,
  the bot path sets `chosen = handSize - 1` unconditionally if the drawn card
  is legal, without any confirmation step.
- Two-player reverse acts like a skip. The REVERSE effect in `CardEffects`
  calls `next()` twice when `playerCount() == 2`.
- Bot priority: draw-two → skip → number → wild → draw.
- Bot color tie-break: R > Y > G > B.
- First up-card is never a wild (re-draw loop preserved in `startGame()`).
- The `calledColor = ""` behavior is now `Card.Color.NONE`. This is a
  representation change, not a behavior change: all callers treat NONE the
  same way the empty string was treated.

## Risks that remain

- `PlayerInputSource` is passed as `NullInputSource` for bot-only games,
  which fails fast on any accidental call. The implicit null contract is gone.
- The safety limit (3000 turns) is documented with a comment explaining
  the reasoning behind the number.
- Scoring is tightly coupled to the win-detection moment inside `GameEngine`.
  A house rule that scores differently (e.g. penalties for holding wilds)
  would require editing the engine directly.
- `GameState.buildAndShuffleDeck()` hard-codes the standard 108-card UNO
  deck. Variant decks require editing the model.
- `CardEffects` uses a static initializer block. If a `Card.Rank` is added
  without registering an effect, `forRank()` throws at runtime rather than
  at compile time. This could be caught earlier by a test that asserts all
  ranks have a registered effect.