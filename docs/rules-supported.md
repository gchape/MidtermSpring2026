# Rules Supported

## Deck Composition

Implemented exactly per the reference: 108 cards, four colors, one 0 per color, two each of 1–9 per color, two
Skip/Reverse/Draw Two per color, four Wilds, four Wild Draw Fours.

## Legal Play Validation

A card is legal when any of these is true:

- It is a Wild or Wild Draw Four (always legal)
- Its color matches the up-card color
- A color was called after a wild and the card matches the called color
- Its rank matches the up-card rank (action type match, non-number only)
- Both cards are numbers and share the same digit

Illegal plays result in a penalty card and loss of turn.

## Skip

The next player loses their turn. Play continues with the player after the skipped one. Works correctly for 2–4 players.

## Reverse

Turn direction flips from clockwise to counterclockwise (or the reverse).

**Two-player variant:** in a two-player game, Reverse acts like Skip — direction still flips, but then `next()` is
called twice, which returns play to the same player. This is consistent with common UNO rule interpretation and is
tested in `SkipReverseTest`.

## Draw Two

The next player draws two cards and loses their turn. No stacking implemented.

## Wild

The player who plays Wild chooses the next active color. Bots pick the color most represented in their remaining hand.
The chosen color governs legal play until another card is played.

## Wild Draw Four

The player chooses the next active color. The next player draws four cards and loses their turn. No challenge rule
implemented.

## Draw and Pass Behavior

On a player's turn, if no card in hand is legal, the player draws one card. If the drawn card is legal, bots play it
immediately. Human players are asked whether to play it. If the drawn card is not legal (or the human declines), the
turn passes. This is tested in `DrawPassTest`.

## UNO Call and Missed-UNO Penalty

When a player's hand drops to one card, the engine asks whether they call UNO:

- **Bots always successfully call UNO.** This is a deliberate simplification — bot games stay deterministic, and a
  bot's hand size alone is enough to know whether it is "at one card," so there is no real decision to model. See
  `BotStrategy.callsUno()`.
- **Human players are genuinely asked**, via a `Call UNO? y/n` prompt the moment their hand reaches one card. A "yes"
  answer announces the call and clears the risk immediately. A "no" answer (or anything other than y/yes) leaves the
  player exposed.
- **The missed-call check fires at the start of the very next turn.** If the player who didn't call is still sitting
  on one card when another player's turn begins, they draw two cards as a penalty. The window closes the moment it
  becomes the same player's own turn again.

This is tested in `UnoCallTest`.

## Round Scoring

When a player empties their hand, they score the sum of all opponents' remaining card values: number cards score face
value, Skip/Reverse/Draw Two score 20, Wild/Wild Draw Four score 50.

## Multi-Round Game to Target Score

The game runs rounds until a player reaches or exceeds the target score (default 500). Scores accumulate across rounds.
The first player to reach the target wins the match.

## Starting Card Behavior

The first up-card is always a non-wild colored card. If a Wild or Wild Draw Four is drawn during setup, it is set aside
and another card is drawn in its place, repeating until a non-wild card appears.

Colored action cards (Skip, Reverse, Draw Two) **are** allowed as the starting up-card. Their effect is not applied
at the start of the round — play simply begins from that card as the current up-card without triggering any action.
This is a deliberate simplification and is documented here.

## Simplifications and Variants

- No Wild Draw Four challenge rule.
- No Draw Two stacking.
- Bots always call UNO automatically; only human players can decline and risk the penalty.
- Starting action cards are allowed but their effect is not triggered at round start.
- Reverse in a two-player game acts like Skip (direction flips, same player goes again).