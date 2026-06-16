package tech.provokedynamic.uno.db.repository;

import org.apache.ibatis.session.SqlSession;
import tech.provokedynamic.uno.db.Database;
import tech.provokedynamic.uno.db.mapper.GameMapper;
import tech.provokedynamic.uno.db.mapper.PlayerMapper;
import tech.provokedynamic.uno.db.model.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Single persistence facade used by the game layer.
 * All SQL goes through MyBatis mappers — no raw JDBC here.
 */
public class GameRepository {

    /**
     * Persists one completed game.
     *
     * @param playerNames  ordered list of player names
     * @param scores       parallel array of per-player cumulative scores for this game
     * @param winnerIndex  index into playerNames of the winner (-1 if safety limit)
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
            gm.insertGame(game);  // sets game.id via useGeneratedKeys

            // Insert per-player result rows
            for (int i = 0; i < playerNames.size(); i++) {
                PlayerRecord player = pm.findByName(playerNames.get(i));
                GamePlayerRecord gp = new GamePlayerRecord();
                gp.setGameId(game.getId());
                gp.setPlayerId(player.getId());
                gp.setScore(scores[i]);
                gp.setWinner(i == winnerIndex);
                gm.insertGamePlayer(gp);
            }

            session.commit();
        }
    }

    /**
     * Returns the N most recently finished games.
     */
    public List<GameRecord> recentGames(int limit) {
        try (SqlSession session = Database.factory().openSession()) {
            return session.getMapper(GameMapper.class).findRecentGames(limit);
        }
    }

    /**
     * Returns all players ranked by number of wins.
     */
    public List<WinCountRecord> winCounts() {
        try (SqlSession session = Database.factory().openSession()) {
            return session.getMapper(PlayerMapper.class).findWinCounts();
        }
    }

    /**
     * Returns all players ranked by total score across all games.
     */
    public List<TopScoreRecord> topScores() {
        try (SqlSession session = Database.factory().openSession()) {
            return session.getMapper(PlayerMapper.class).findTopScores();
        }
    }
}
