package tech.provokedynamic.uno.db.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GameRecord {
    private int id;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private int roundsPlayed;
    private List<GamePlayerRecord> players;
}
