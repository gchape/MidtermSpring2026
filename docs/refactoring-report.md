# Refactoring Report

## What behavior was characterized before refactoring

Characterization tests were written first, covering the behaviors most at
risk during structural changes:

- Legality rules: match by color, number, action rank, wild, called color,
  and the illegal combinations.
- Bot priority order: draw-two before skip, skip before number, number
  before wild, draw when nothing is legal. This order is a quirk of the
  original `chooseBotCard()` and the tests pin it exactly.
- Bot color selection: most-frequent color wins; R > Y > G > B tie-break.
- Scoring: `handPoints` sums opponent card values correctly.
- Deck reshuffle: drawing from an empty deck consumes the discard pile.
- Skip, reverse, draw-two effects on `currentPlayer`.
- Two-player reverse acting like skip (original quirk preserved).
- Full bot-only game completing without hitting the safety limit.

## Worst design problems found

1. **Duplicated legality check.** The `isLegal()` method existed but was
   not used by `chooseBotCard()` or by the inline check inside `playGame()`.
   Three independent copies drifted apart silently.

2. **Primitive card strings.** Every piece of code re-parsed `"R+2"` or
   `"W4"` from scratch. `color()`, `rank()`, `number()`, and `points()`
   were all free functions operating on strings, scattered through Main.

3. **Console I/O tangled into the game loop.** The `~200`-line `playGame()`
   method called `IO.println` directly throughout, making it impossible to
   run a simulated game (e.g. for a bot tournament) without producing output,
   and impossible to test turn outcomes without capturing stdout.

4. **Global mutable state.** All state (`deck`, `hands`, `scores`, etc.)
   lived in `static` fields. Tests had to reset global state manually, and
   any two concurrent games would corrupt each other.

5. **Bot strategy mixed with rule knowledge.** `chooseBotCard()` re-implemented
   the legality check inline rather than calling `isLegal()`, coupling
   strategy decisions to rule knowledge.

## Refactoring steps performed

Each step was followed by running the characterization tests before
proceeding to the next.

### Step 1 — Write characterization tests

Added `UnoCharacterizationTest` covering the behaviors listed above.
No production code was changed in this step.

### Step 2 — Extract `Card` value object

Replaced raw string cards (`"R+2"`, `"W4"`) with a `Card` record.
`Card` owns `color()`, `rank()`, `number()`, `points()`, and `isWild()`.
All parsing is now in one place. The original free functions were deleted.

Tests still passed after this step.

### Step 3 — Extract `Rules.isLegal()`

Created `Rules` as the single authoritative home for the legality check.
All three callers now delegate here. The duplicate copies in `playGame()`
and `chooseBotCard()` were removed.

String overload preserved for callers not yet using `Card`.

### Step 4 — Extract `GameState`

Bundled all global `static` fields into a `GameState` object. Tests
construct `GameState` directly, set up hands and the up-card, and verify
behavior without touching the CLI.

### Step 5 — Extract `BotStrategy`

Moved `chooseBotCard()` and `chooseBotColor()` into `BotStrategy`.
Now delegates to `Rules.isLegal()` — no more inline duplication.
Priority order preserved exactly (characterization tests verify this).

### Step 6 — Separate console I/O with `GameView` / `PlayerInputSource`

Introduced `GameView` interface and two implementations: `ConsoleView`
(identical output to original) and `SilentView` (replaces the `quiet` flag).

Introduced `PlayerInputSource` interface and `ConsoleInput` implementation.

The game loop in `GameEngine` calls only interfaces — no `IO.println`,
no `Scanner` directly.

### Step 7 — Extract `GameEngine`

Moved the turn loop out of `Main` into `GameEngine`. `Main` is now a thin
argument parser that wires the pieces together.

## Behavior intentionally preserved

- All hands are visible in the terminal during human play.
- Humans may type `draw` even when holding a legal card.
- An out-of-range index causes a penalty card and turn loss.
- Bot players automatically play a drawn card when legal.
- Two-player reverse acts like a skip (double `next()`).
- Bot priority: draw-two → skip → number → wild → draw.
- Bot color tie-break: R > Y > G > B.
- First up-card is never a wild (re-draw loop preserved).

## Risks that remain

- `GameState` fields are package-visible for test convenience. A future
  accessor layer would be cleaner.
- `PlayerInputSource` is passed as `null` for bot-only games and checked
  implicitly (bots never call it). An explicit `BotInputSource` no-op
  would be safer.
- The safety limit (3000 turns) is a hard-coded constant with no
  explanation of why that number was chosen.
- Scoring is still tightly coupled to the win-detection moment inside
  `GameEngine`; a separate `Scorer` would make multi-round scoring
  strategies easier to vary.