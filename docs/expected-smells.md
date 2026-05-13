# Expected Smells

The starting code contains the following design problems:

* one large `tech.provokedynamic.tech.provokedynamic.uno.Main` class
* global mutable state
* long game loop
* mixed CLI and game logic
* duplicated legality checks
* primitive-heavy card representation
* switch/if-heavy action handling
* hidden randomness
* weak player boundaries
* parsing mixed with validation
* scoring mixed with game completion
* bot decisions mixed with rule knowledge

Students should discover these through tests and extension pressure, not by applying a specific pattern first.