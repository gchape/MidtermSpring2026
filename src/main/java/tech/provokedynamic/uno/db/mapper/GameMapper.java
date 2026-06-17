package tech.provokedynamic.uno.db.mapper;

import tech.provokedynamic.uno.db.model.GameRecord;

import java.util.List;
import java.util.Map;

public interface GameMapper {

    void insertGame(GameRecord game);

    void updateGame(GameRecord game);

    void insertGamePlayer(Map<String, Object> params);

    List<GameRecord> findRecentGames(int limit);
}
