package tech.provokedynamic.uno.db.mapper;

import tech.provokedynamic.uno.db.model.PlayerRecord;
import tech.provokedynamic.uno.db.model.TopScoreRecord;
import tech.provokedynamic.uno.db.model.WinCountRecord;

import java.util.List;

public interface PlayerMapper {

    void insertIfAbsent(String name);

    PlayerRecord findByName(String name);

    List<WinCountRecord> findWinCounts();

    List<TopScoreRecord> findTopScores();
}
