package io.joshatron.tak.cli.app.local;

import io.joshatron.tak.engine.exception.TakEngineException;
import io.joshatron.tak.engine.game.GameState;
import io.joshatron.tak.engine.game.Player;

public class Tutorial {

    public static void play() {
        try {
            GameState testState = new GameState(Player.WHITE, 5);

            System.out.println("Welcome to the Tak tutorial. Here you will learn the basics for how to play the game of Tak.");
        }
        catch(TakEngineException e) {
            System.out.println("Something went wrong with the engine.");
        }
    }
}
