package tech.provokedynamic.uno.db.mapper;

import org.apache.ibatis.annotations.Param;
import tech.provokedynamic.uno.db.model.GamePlayerRecord;
import tech.provokedynamic.uno.db.model.GameRecord;

import java.time.LocalDateTime;
import java.util.List;

public interface GameMapper {

    void insertGame(GameRecord game);

    void updateGame(@Param("id") int id,
                    @Param("finishedAt") LocalDateTime finishedAt,
                    @Param("roundsPlayed") int roundsPlayed);

    void insertGamePlayer(GamePlayerRecord gp);

    /**
     * Returns the N most recently finished games with their players.
     */
    List<GameRecord> findRecentGames(@Param("limit") int limit);
}
