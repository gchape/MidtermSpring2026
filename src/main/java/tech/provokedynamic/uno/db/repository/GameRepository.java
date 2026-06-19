package tech.provokedynamic.uno.db.repository;

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
 * Persists game results and retrieves history/statistics.
 * Each public method opens its own session and commits or rolls back.
 */
public class GameRepository {

    /**
     * Persists a completed game.
     *
     * @param playerNames  ordered list of player names
     * @param scores       scores[i] corresponds to playerNames.get(i)
     * @param winnerIndex  index of the winning player, or -1 if safety limit hit
     * @param roundsPlayed number of rounds played
     * @param startedAt    when the game started
     */
    public void saveGame(List<String> playerNames,
                         int[] scores,
                         int winnerIndex,
                         int roundsPlayed,
                         LocalDateTime startedAt) {

        try (SqlSession session = Database.factory().openSession()) {
            PlayerMapper pm = session.getMapper(PlayerMapper.class);
            GameMapper gm = session.getMapper(GameMapper.class);

            // Upsert all players
            for (String name : playerNames) {
                pm.insertIfAbsent(name);
            }

            // Insert game row
            GameRecord game = new GameRecord();
            game.setStartedAt(startedAt);
            game.setFinishedAt(LocalDateTime.now());
            game.setRoundsPlayed(roundsPlayed);
            gm.insertGame(game);  // id is populated via useGeneratedKeys

            // Insert one game_players row per player
            for (int i = 0; i < playerNames.size(); i++) {
                PlayerRecord player = pm.findByName(playerNames.get(i));
                Map<String, Object> params = new HashMap<>();
                params.put("gameId", game.getId());
                params.put("playerId", player.getId());
                params.put("score", scores[i]);
                params.put("winner", i == winnerIndex);
                gm.insertGamePlayer(params);
            }

            session.commit();
        }
    }

    /**
     * Returns the {@code limit} most recently finished games, newest first.
     */
    public List<GameRecord> recentGames(int limit) {
        try (SqlSession session = Database.factory().openSession()) {
            return session.getMapper(GameMapper.class).findRecentGames(limit);
        }
    }

    /**
     * Returns all players ranked by number of wins, descending.
     */
    public List<WinCountRecord> winCounts() {
        try (SqlSession session = Database.factory().openSession()) {
            return session.getMapper(PlayerMapper.class).findWinCounts();
        }
    }

    /**
     * Returns all players ranked by cumulative score, descending.
     */
    public List<TopScoreRecord> topScores() {
        try (SqlSession session = Database.factory().openSession()) {
            return session.getMapper(PlayerMapper.class).findTopScores();
        }
    }
}
