package tech.provokedynamic.uno.db.repository;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import tech.provokedynamic.uno.db.Database;
import tech.provokedynamic.uno.db.mapper.GameMapper;
import tech.provokedynamic.uno.db.mapper.PlayerMapper;
import tech.provokedynamic.uno.db.model.GameRecord;
import tech.provokedynamic.uno.db.model.PlayerRecord;
import tech.provokedynamic.uno.db.model.TopScoreRecord;
import tech.provokedynamic.uno.db.model.WinCountRecord;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Single persistence facade used by the application layer.
 * <p>
 * Each public method opens, uses, and closes its own {@link SqlSession}.
 * Write operations commit explicitly; read operations are auto-closed
 * without a commit (read-only).
 */
@Slf4j
public class GameRepository {

    private static Map<String, Object> gamePlayerParams(int gameId, int playerId,
                                                        int score, boolean winner) {
        Map<String, Object> params = new HashMap<>(4);
        params.put("gameId", gameId);
        params.put("playerId", playerId);
        params.put("score", score);
        params.put("winner", winner);
        return params;
    }

    /**
     * Persists a completed match in a single transaction.
     * <ol>
     *   <li>Upsert each player by name (idempotent).</li>
     *   <li>Insert the {@code games} row and retrieve the auto-generated id.</li>
     *   <li>Insert one {@code game_players} row per player.</li>
     *   <li>Commit.</li>
     * </ol>
     *
     * @param playerNames  ordered player names
     * @param scores       {@code scores[i]} corresponds to {@code playerNames.get(i)}
     * @param winnerIndex  winning player index, or -1 if the safety limit was hit
     * @param roundsPlayed total rounds played in this match
     * @param startedAt    match start timestamp
     */
    public void saveGame(List<String> playerNames,
                         int[] scores,
                         int winnerIndex,
                         int roundsPlayed,
                         LocalDateTime startedAt) {

        log.info("Saving game: players={}, rounds={}, winner={}",
                playerNames, roundsPlayed,
                winnerIndex >= 0 ? playerNames.get(winnerIndex) : "none (safety limit)");

        try (SqlSession session = Database.factory().openSession()) {
            PlayerMapper pm = session.getMapper(PlayerMapper.class);
            GameMapper gm = session.getMapper(GameMapper.class);

            for (String name : playerNames) {
                pm.insertIfAbsent(name);
            }

            GameRecord game = new GameRecord();
            game.setStartedAt(startedAt);
            game.setFinishedAt(LocalDateTime.now());
            game.setRoundsPlayed(roundsPlayed);
            gm.insertGame(game);

            log.debug("Inserted games row with id={}", game.getId());

            for (int i = 0; i < playerNames.size(); i++) {
                PlayerRecord player = pm.findByName(playerNames.get(i));
                gm.insertGamePlayer(gamePlayerParams(game.getId(), player.getId(), scores[i], i == winnerIndex));
                log.debug("Inserted game_players row: player={}, score={}, winner={}",
                        player.getName(), scores[i], i == winnerIndex);
            }

            session.commit();
            log.info("Game saved successfully (id={})", game.getId());
        }
    }

    /**
     * Returns the {@code limit} most recently finished games, newest first,
     * each with its player rows attached.
     */
    public List<GameRecord> recentGames(int limit) {
        log.debug("Querying recent games (limit={})", limit);
        try (SqlSession session = Database.factory().openSession()) {
            List<GameRecord> results = session.getMapper(GameMapper.class).findRecentGames(limit);
            log.debug("Found {} recent game(s)", results.size());
            return results;
        }
    }

    /**
     * Returns all players ranked by win count, descending.
     */
    public List<WinCountRecord> winCounts() {
        log.debug("Querying win counts");
        try (SqlSession session = Database.factory().openSession()) {
            return session.getMapper(PlayerMapper.class).findWinCounts();
        }
    }

    /**
     * Returns all players ranked by cumulative score, descending.
     */
    public List<TopScoreRecord> topScores() {
        log.debug("Querying top scores");
        try (SqlSession session = Database.factory().openSession()) {
            return session.getMapper(PlayerMapper.class).findTopScores();
        }
    }
}
