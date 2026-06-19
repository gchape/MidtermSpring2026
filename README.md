## Assignment 5 — UNO CLI

Game results are automatically saved to an embedded H2 database (`data/uno.mv.db`).
No database server installation is required.

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

### Configure credentials (optional)
The embedded H2 database uses safe defaults (`sa` / empty password).
To override:
```bash
export DB_USERNAME=myuser
export DB_PASSWORD=mypassword
java -jar target/uno.jar
```

### Run persistence tests
```bash
mvn test -Dtest=GameRepositoryTest
```

Tests use an isolated in-memory H2 database — no files written, no external setup required.

For full database documentation see [docs/database.md](docs/database.md).