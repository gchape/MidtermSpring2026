# Refactoring Guide

Start with behavior preservation.

## Suggested Order

1. Compile and run the game.
2. Run the characterization checks.
3. Add checks around the behavior you plan to change.
4. Extract small methods from the game loop.
5. Separate user input parsing from move validation.
6. Centralize legal-play rules.
7. Isolate card effects.
8. Add one extension.

## Useful Refactorings

* Extract Method
* Extract Class
* Move Method
* Split Phase
* Replace Conditional with Polymorphism
* Introduce Parameter Object
