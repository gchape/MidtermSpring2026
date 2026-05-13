# Extension Readiness Note

## Which extension this design supports best

**Adding a smarter bot strategy.**

The current `BotStrategy` is a simple priority list (draw-two → skip →
number → wild). A realistic next step would be a strategy that tracks
which cards opponents have played, estimates hand sizes, and holds wilds
until tactically useful.

## Where the change would be implemented

`BotStrategy.chooseCard()` is the single method that makes card-play
decisions. To add a smarter strategy:

1. Introduce a `Strategy` interface with `chooseCard()` and `chooseColor()`.
2. Make the current priority-list logic the `DefaultStrategy` implementation.
3. Implement `SmartStrategy` with access to `GameState` (visible hand sizes,
   discard pile history) to inform decisions.
4. Pass the chosen strategy into `GameEngine` at construction time.

No changes to `GameEngine`, `Rules`, `Card`, or `GameView` are needed.
The seam is clean.

Characterization tests already pin `DefaultStrategy` behavior, so the
existing test suite continues to verify that the default bot works
correctly while new strategy tests cover the smarter variant.

## What still makes change difficult

- **Scoring is inside `GameEngine`.** Computing and recording points
  happens inline at the win-detection moment. A variant that scores
  differently (e.g. negative points for holding a wild) would require
  editing the engine rather than swapping a `Scorer`.

- **Deck composition is inside `GameState.buildAndShuffleDeck()`.** Adding
  a house rule (e.g. remove all draw-two cards, or add custom cards) means
  editing `GameState`. A `DeckFactory` interface would make this a
  one-class change.

- **No replay log.** The `GameView` interface receives events but there is
  no way to record and replay a game sequence. Adding a `RecordingView`
  decorator would be straightforward, but the `PlayerInputSource` side
  (human input) has no equivalent logging, so full replay would require
  additional work there.