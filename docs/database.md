# Database Documentation

## Selected Tools

| Layer    | Technology                   |
|----------|------------------------------|
| Database | H2 (embedded, file mode)     |
| ORM      | MyBatis 3.5.x                |
| Test DB  | H2 (in-memory, per test run) |

H2 runs embedded inside the JVM — no database server to install or start.
Data is stored in `data/uno.mv.db` next to the working directory by default.

---

## Schema

Defined in `src/main/resources/db/schema.sql`.
Applied automatically on every startup using `CREATE IF NOT EXISTS` — safe to re-run.

```text
players
(id PK, name UNIQUE)

games        (id PK, started_at, finished_at, rounds_played)
game_players (game_id FK, player_id FK, score, is_winner)
```

**Relationships:**

- One game has many `game_players` rows (one per player in that game).
- Each `game_players` row references one `players` row.
- Players are upserted by name on each game save — no duplicates.

**A note on "rounds":** one `games` row represents one full match — players play
repeated rounds (each round ends when someone empties their hand) until a
player reaches the target score. `rounds_played` is the count of those
individual rounds within the match; we do not persist a separate row per
round, only the running per-player scores and the eventual winner. This is a
deliberate simplification, not an oversight.

---

## ORM Configuration

`src/main/resources/mybatis-config.xml` configures MyBatis with:

- `mapUnderscoreToCamelCase = true` (e.g. `started_at` → `startedAt`)
- Two mapper XML files: `db/PlayerMapper.xml`, `db/GameMapper.xml`
- Connection properties injected at runtime via `Database.init(Properties)`

No credentials are hardcoded in source. Connection URLs are built by
`Database.h2FileProps()` / `Database.h2MemProps()` at startup.

---

## Running the Application with Persistence

Persistence is **on by default**. Schema setup runs once at startup
(`Database.init()` + `SchemaInit.run()`), and the completed match is saved
once a player reaches the target score.

Default (H2 file DB at `data/uno`):

```bash
./scripts/run.sh --bots 3 --target 500 --quiet
```

Custom DB path:

```bash
./scripts/run.sh --db-path /tmp/myuno --target 500 --quiet
```

Disable persistence entirely (no schema setup, no save, `--report` unavailable):

```bash
./scripts/run.sh --no-db --target 500
```

---

## Viewing Game History and Statistics

```bash
./scripts/run.sh --report
```

This prints three sections to stdout:

1. **Recent Games** — last 10 games with per-player scores and winner marked ★
2. **Player Win Counts** — all players ranked by number of wins
3. **Highest Total Scores** — all players ranked by cumulative score

With a custom DB path:

```bash
./scripts/run.sh --db-path /tmp/myuno --report
```

`--report` and `--no-db` are mutually exclusive — there's nothing to report
if persistence was disabled.

---

## Running Persistence Tests

```bash
mvn test
```

Persistence tests are in `GameRepositoryTest`. They use H2 in-memory mode —
no file is created, no external database is required. Each test gets its
own uniquely named in-memory database to prevent cross-test pollution.

To run only persistence tests:

```bash
mvn test -Dtest=GameRepositoryTest
```

---

## Mapper Files

| File                  | Purpose                                                     |
|-----------------------|-------------------------------------------------------------|
| `db/PlayerMapper.xml` | Player upsert, lookup, win counts, top scores               |
| `db/GameMapper.xml`   | Game insert/update, game_players insert, recent games query |

---

## Data Directory

At runtime, H2 creates `data/uno.mv.db` in the working directory (or wherever
`--db-path` points). This file is the persistent store and should be added
to `.gitignore`.

```
data/
```