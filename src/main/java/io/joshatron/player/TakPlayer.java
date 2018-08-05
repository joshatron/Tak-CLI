package io.joshatron.player;

import io.joshatron.engine.GameState;
import io.joshatron.engine.Turn;

public interface TakPlayer {

    public Turn getTurn(GameState state);
}
