package tech.provokedynamic.uno.db.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GamePlayerRecord {
    private int playerId;
    private String playerName;
    private int score;
    private boolean winner;
}
