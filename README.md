## Persistence (Assignment 5)

Game results are automatically saved to an embedded H2 database (`data/uno.mv.db`).

### View game history and statistics

```bash
java -jar target/uno.jar --report
```

### Run with a custom database path

```bash
java -jar target/uno.jar --db-path /tmp/myuno --games 5 --quiet
```

### Disable the database

```bash
java -jar target/uno.jar --no-db --games 5
```

### Run persistence tests

```bash
mvn test -Dtest=GameRepositoryTest
```

For full database documentation see [docs/database.md](docs/database.md).