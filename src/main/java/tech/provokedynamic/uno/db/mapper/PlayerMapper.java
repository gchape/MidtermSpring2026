package tech.provokedynamic.uno.db.mapper;

import org.apache.ibatis.annotations.Param;
import tech.provokedynamic.uno.db.model.PlayerRecord;
import tech.provokedynamic.uno.db.model.TopScoreRecord;
import tech.provokedynamic.uno.db.model.WinCountRecord;

import java.util.List;

public interface PlayerMapper {

    void insertIfAbsent(@Param("name") String name);

    PlayerRecord findByName(@Param("name") String name);

    List<WinCountRecord> findWinCounts();

    List<TopScoreRecord> findTopScores();
}
