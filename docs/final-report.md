# Final Report

## Rules Implemented

All ten rule features from the reference are implemented:

1. **Deck composition** — 108-card standard UNO deck built and shuffled in `GameState.buildAndShuffleDeck()`.
2. **Legal play validation** — `Rules.isLegal()` covers color match, number match, action-type match, wild always legal,
   and called-color-after-wild.
3. **Skip** — `CardEffects` advances the turn twice, skipping the next player.
4. **Reverse** — `CardEffects` flips `direction`; in a two-player game a double-advance makes it act like Skip.
5. **Draw Two** — next player draws two cards and loses their turn.
6. **Wild** — player (or bot) chooses the next active color; affects all subsequent legality checks.
7. **Wild Draw Four** — color choice + four-card penalty + skip for the next player.
8. **Draw and pass** — player draws one card on no legal play; bots play it if legal; human is asked; turn advances
   regardless.
9. **UNO call and missed-UNO penalty** — UNO is detected the instant a player's hand drops to one card. Bots always
   call it successfully. Human players are genuinely prompted (`Call UNO? y/n`); declining leaves them exposed, and if
   they're still on one card when the next player's turn begins, they draw two as a penalty.
10. **Round scoring and multi-round target** — winner scores opponents' remaining card values; rounds repeat until a
    player reaches the target (default 500).

## How to Play from the CLI

```sh
# Bot-only game to 500 points
./scripts/run.sh

# Human vs 2 bots to 200 points
./scripts/run.sh --human --bots 2 --target 200
```

On each turn the up-card and your hand are displayed with indices. Type an index (e.g. `2`) or a card code (e.g. `R5`,
`GS`, `W`) to play, or `DRAW` to draw. After playing a Wild, you are prompted to call a color (`R`, `Y`, `G`, `B`). If
your hand ever drops to one card, you are prompted to call UNO — declining risks a two-card penalty on the next turn.

Game results are saved automatically (H2 + MyBatis) once a match ends. Run `./scripts/run.sh --report` to see recent
games, win counts, and high scores, or `./scripts/run.sh --no-db` to play without persistence. See `docs/database.md`
for the full set of persistence-related flags.

## Architecture

The project separates concerns into five layers:

- **Model** (`GameState`, `Card`) — pure data, no I/O, no logic.
- **Rules** (`Rules`) — stateless, static methods; independently testable.
- **Game** (`GameEngine`, `CardEffects`) — the game loop and card effects. Depends only on interfaces (`GameView`,
  `PlayerInputSource`), never on `Scanner` or `IO` directly.
- **Bot** (`BotStrategy`) — pure functions, no state.
- **View / Input** (`ConsoleView`, `SilentView`, `ConsoleInput`, `NullInputSource`) — the only layer that touches I/O.

This means every game rule can be tested without a terminal: inject `SilentView` and `NullInputSource` (or a scripted
`PlayerInputSource` for human-seat scenarios), manipulate `GameState` directly, and call `GameEngine` methods.

`GameState` also exposes one test-only seam, `forceNextDraw(Card)`, which places a card on top of the deck so a test
can force a specific draw outcome deterministically instead of depending on shuffle timing. Production code never
calls it.

## Tests Added

| Test class                   | Rubric area                                                                                                                                                                    |
|------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `DeckCompositionTest`        | 1.1 — deck composition                                                                                                                                                         |
| `CardTest`                   | 1.1 — card parsing, points, and `toString()` round-trip                                                                                                                        |
| `RulesTest`, `LegalPlayTest` | 1.2 — legal play validation (color, number, action type, wilds, called color)                                                                                                  |
| `SkipReverseTest`            | 1.3, 1.4 — Skip and Reverse, including the two-player Reverse variant                                                                                                          |
| `DrawTwoWildTest`            | 1.5, 1.6, 1.7 — Draw Two, Wild, Wild Draw Four, including engine-level integration tests confirming a played Wild/Wild Draw Four actually sets the called color on `GameState` |
| `DrawPassTest`               | 1.8 — draw and pass behavior                                                                                                                                                   |
| `UnoCallTest`                | 1.9 — UNO detection, a successful call avoiding the penalty, and a declined call triggering it                                                                                 |
| `BotStrategyTest`            | bot card/color choice priority, and `BotStrategy.callsUno()`                                                                                                                   |
| `ScoringMultiRoundTest`      | 1.10 — scoring, score accumulation, target detection                                                                                                                           |
| `GameStateTest`              | 2.1 — deck/hand/turn primitives in isolation, no engine involved                                                                                                               |
| `GameEngineTest`             | 2.1 — full-game integration, illegal-play penalties, and a rigged-deck test confirming the engine really auto-plays a legal drawn card                                         |
| `GameRepositoryTest`         | A5 persistence — isolated per-test in-memory H2 database, no shared state                                                                                                      |

## Limitations

- No Wild Draw Four challenge rule.
- No Draw Two stacking.
- Bot strategy is greedy and simple (prefers Draw Two > Skip > Number > Wild).
- Bots never miss a UNO call — only a human player can decline the call and risk the penalty. This is a documented
  simplification (see `docs/rules-supported.md`), not a gap in the mechanic itself: the call/decline choice is real
  for the player who can actually make it.
- The missed-UNO penalty applies only at the start of the next player's turn; a player who draws during their own
  turn is not re-checked mid-turn.