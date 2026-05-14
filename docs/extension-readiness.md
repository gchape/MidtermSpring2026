# Extension readiness note

## Which extension this design supports best

**Adding a smarter bot strategy.**

The current `BotStrategy` is a fixed priority list (draw-two -> skip ->
number -> wild). A realistic next step would be a strategy that tracks
which cards opponents have played, estimates hand sizes, and holds wilds
until tactically useful.

## Where the change would be implemented

The seam is clean and confined to a single class. The steps required are:

1. Introduce a `Strategy` interface in the `bot` package:

```java
public interface Strategy {
    int chooseCard(List<Card> hand, Card upCard, Card.Color calledColor);

    Card.Color chooseColor(List<Card> hand);
}
```

2. Rename the current logic to `DefaultStrategy implements Strategy`.
   No changes to its behavior — characterization tests continue to pass.

3. Implement `SmartStrategy implements Strategy`, which can receive a
   read-only view of `GameState` (hand sizes, discard pile) via its
   constructor to inform decisions.

4. Pass the chosen `Strategy` into `GameEngine` at construction time,
   replacing the static `BotStrategy.chooseCard()` calls.

No changes are needed in `GameEngine`, `Rules`, `Card`, `GameView`, or
`GameState` to support this extension. The characterization tests already
pin `DefaultStrategy` behavior so regressions are caught automatically.

## What still makes change difficult

**Scoring is inside `GameEngine`.**
Point calculation and recording happen inline at the win-detection moment
inside `handlePlay()`. A variant scoring rule (e.g. negative points for
holding a wild, or a points cap per round) requires editing `GameEngine`
rather than substituting a `Scorer`. Extracting a `Scorer` interface with
a `computePoints(int winner, GameState state)` method would make this a
one-class swap.

**Deck composition is inside `GameState.buildAndShuffleDeck()`.**
Adding a house-rule variant (remove all draw-two cards, add custom cards,
or use a shorter deck for testing) means editing `GameState`. A `DeckFactory`
interface — `DeckFactory.buildDeck()` returning a `List<Card>` — would
make deck composition a constructor argument and leave `GameState` unchanged.
This would also make testing much easier: tests could inject a known deck
instead of relying on shuffled randomness.

**No replay log.**
`GameView` receives events but there is no way to record and replay a game
sequence for debugging, replay viewing, or AI training data. Adding a
`RecordingView` decorator around `ConsoleView` or `SilentView` would be
straightforward on the output side. However, `PlayerInputSource` has no
equivalent event stream, so full deterministic replay of human games would
require additional work: a `RecordingInput` wrapper that logs each decision
and a `ReplayInput` that plays them back.