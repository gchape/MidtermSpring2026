# Database Documentation

## Overview

UNO uses **H2** as its embedded database and **MyBatis** as the persistence mapper.
No external database server is required for development, testing, or normal play.

---

## Selected Technologies

| Concern     | Technology                                                                                    |
|-------------|-----------------------------------------------------------------------------------------------|
| Database    | H2 (embedded, file-backed in prod; in-memory for tests)                                       |
| Persistence | MyBatis 3 (XML mapper + repository pattern)                                                   |
| Schema init | `SchemaInit` runs `db/schema.sql` on every startup via `CREATE IF NOT EXISTS`                 |
| Credentials | Read from `DB_USERNAME` / `DB_PASSWORD` env vars; defaults to `sa` / `` (H2 embedded default) |

### Why H2?

- Zero installation — ships inside the JAR.
- `AUTO_SERVER=TRUE` allows multiple JVM processes to share the same file database if needed.
- Identical SQL dialect used in both production (file mode) and tests (in-memory mode), so tests reflect real behaviour.

---

## Schema

Located at `src/main/resources/db/schema.sql`.

```sql
CREATE TABLE IF NOT EXISTS players
(
    id
    INTEGER
    PRIMARY
    KEY
    AUTO_INCREMENT,
    name
    VARCHAR
(
    64
) NOT NULL UNIQUE
    );

CREATE TABLE IF NOT EXISTS games
(
    id
    INTEGER
    PRIMARY
    KEY
    AUTO_INCREMENT,
    started_at
    TIMESTAMP
    NOT
    NULL,
    finished_at
    TIMESTAMP,
    rounds_played
    INTEGER
    NOT
    NULL
    DEFAULT
    0
);

CREATE TABLE IF NOT EXISTS game_players
(
    game_id
    INTEGER
    NOT
    NULL
    REFERENCES
    games
(
    id
),
    player_id INTEGER NOT NULL REFERENCES players
(
    id
),
    score INTEGER NOT NULL DEFAULT 0,
    is_winner BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY
(
    game_id,
    player_id
)
    );
```

### What is stored

| Data point       | Table / Column           |
|------------------|--------------------------|
| Player names     | `players.name`           |
| Game start time  | `games.started_at`       |
| Game end time    | `games.finished_at`      |
| Rounds played    | `games.rounds_played`    |
| Per-player score | `game_players.score`     |
| Winner flag      | `game_players.is_winner` |

---

## ORM / MyBatis Configuration

Configuration file: `src/main/resources/mybatis-config.xml`

Key settings:

- `mapUnderscoreToCamelCase=true` — database columns like `started_at` map automatically to Java fields like
  `startedAt`.
- `POOLED` data source — MyBatis manages a connection pool.
- Transaction manager: `JDBC` — commits are explicit (`session.commit()`).

Mappers registered:

- `db/PlayerMapper.xml` → `PlayerMapper` interface
- `db/GameMapper.xml`   → `GameMapper` interface

---

## Credentials

Credentials are **never hardcoded**. They are read from environment variables at startup:

| Variable      | Default (H2 embedded) | Purpose           |
|---------------|-----------------------|-------------------|
| `DB_USERNAME` | `sa`                  | Database username |
| `DB_PASSWORD` | *(empty string)*      | Database password |

For the embedded H2 database used here these defaults are safe — H2 in file mode is not network-accessible by default.
If you switch to a networked database (PostgreSQL, MySQL), set the env vars before running:

```bash
export DB_USERNAME=myuser
export DB_PASSWORD=mypassword
java -jar uno.jar
```

---

## Schema Initialisation

`SchemaInit.run()` is called automatically in `Main` before any game starts (unless `--no-db` is used). It executes
`db/schema.sql` using MyBatis's `ScriptRunner`. All statements use `CREATE TABLE IF NOT EXISTS`, making it safe to call
on every startup.

No manual migration step is required.

---

## Running the Application with Persistence

```bash
# Default: saves to ./data/uno (H2 file database)
java -jar uno.jar

# Custom database path
java -jar uno.jar --db-path /path/to/mydb

# Bot-only, 5 games, quiet output
java -jar uno.jar --bots 3 --games 5 --quiet

# Disable persistence entirely (no DB touched)
java -jar uno.jar --no-db
```

---

## Viewing Game History and Statistics

Run with the `--report` flag after one or more games have been played:

```bash
java -jar uno.jar --report
```

Output includes three sections:

### Recent Games (last 10)

Lists the most recently finished games with each player's score and a ★ next to the winner.

```
=== Recent Games (last 10) ===
  Game #3  started=2025-06-01T14:32:00  rounds=12
    Bot1          score=120 ★
    Bot2          score=0
    Bot3          score=45
```

### Player Win Counts

All players ranked by total number of game wins.

```
=== Player Win Counts ===
  Bot1          3 win(s)
  Bot3          1 win(s)
```

### Highest Total Scores

All players ranked by the sum of scores across all games.

```
=== Highest Total Scores ===
  Bot1          340 pts
  Bot3          90 pts
  Bot2          0 pts
```

---

## Running Persistence Tests

Tests use an **isolated in-memory H2 database** — no external setup, no files written to disk.

```bash
# Run all tests (including persistence tests)
mvn test

# Run only the persistence test class
mvn test -Dtest=GameRepositoryTest
```

`GameRepositoryTest` covers:

| Test                                             | What it verifies                                |
|--------------------------------------------------|-------------------------------------------------|
| `saveGame_persistsAllRequiredFields`             | rounds, timestamps, and player rows are written |
| `saveGame_marksCorrectWinner`                    | the `is_winner` flag is set on the right player |
| `saveGame_persistsPerPlayerScores`               | each player's score is stored correctly         |
| `recentGames_respectsLimit`                      | `LIMIT N` is honoured                           |
| `recentGames_returnsEmptyListWhenNoGames`        | graceful empty result                           |
| `winCounts_returnsCorrectWinnerTally`            | win aggregation is accurate                     |
| `winCounts_orderedByWinsDescending`              | result ordering is correct                      |
| `topScores_returnsAllPlayersWithAggregatedScore` | scores are summed across games                  |
| `topScores_orderedByTotalScoreDescending`        | result ordering is correct                      |
| `saveGame_samePlayerNameTwiceDoesNotDuplicate`   | `MERGE` upsert is idempotent                    |
| `saveGame_safetyLimitGamePersistsWithNoWinner`   | `-1` winner index stores no winner              |

---

## Repository / DAO Layer

All SQL lives in `src/main/resources/db/GameMapper.xml` and `PlayerMapper.xml`.
The game logic (`GameEngine`, `Main`) never contains raw SQL.

```
GameRepository          ← single persistence facade used by Main
  ├── GameMapper         ← insert/update games and game_players rows
  └── PlayerMapper       ← upsert players; query win counts and top scores
```

`GameRepository.saveGame()` performs everything in a single transaction:

1. Upsert each player by name (idempotent).
2. Insert the `games` row and retrieve the auto-generated `id`.
3. Insert one `game_players` row per player with the per-game score delta and winner flag.
4. Commit.