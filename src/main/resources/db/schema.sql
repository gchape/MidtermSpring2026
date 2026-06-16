CREATE TABLE IF NOT EXISTS players (
    id   INTEGER PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS games (
    id           INTEGER PRIMARY KEY AUTO_INCREMENT,
    started_at   TIMESTAMP NOT NULL,
    finished_at  TIMESTAMP,
    rounds_played INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS game_players (
    game_id   INTEGER NOT NULL REFERENCES games(id),
    player_id INTEGER NOT NULL REFERENCES players(id),
    score     INTEGER NOT NULL DEFAULT 0,
    is_winner BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (game_id, player_id)
);