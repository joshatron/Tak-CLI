package io.joshatron.tak.cli.app.server.response;

import io.joshatron.tak.engine.game.Player;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GameInfo {
    private String gameId;
    private String white;
    private String black;
    private int size;
    private Player first;
    private Player current;
    private Player winner;
    private boolean done;
    private String[] turns;
}
