package tech.provokedynamic.uno.db.model;

import lombok.Data;

@Data
public class GamePlayerRecord {
    private int gameId;
    private int playerId;
    private String playerName;
    private int score;
    private boolean winner;
}
