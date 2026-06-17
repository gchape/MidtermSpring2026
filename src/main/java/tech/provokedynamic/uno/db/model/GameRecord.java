package tech.provokedynamic.uno.db.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class GameRecord {
    private int id;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private int roundsPlayed;
    private List<GamePlayerRecord> players = new ArrayList<>();
}
