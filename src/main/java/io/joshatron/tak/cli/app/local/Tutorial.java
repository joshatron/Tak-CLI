package io.joshatron.tak.cli.app.local;

import io.joshatron.tak.engine.exception.TakEngineException;
import io.joshatron.tak.engine.game.GameState;
import io.joshatron.tak.engine.game.Player;

public class Tutorial {

    public static void play() {
        try {
            GameState testState = new GameState(Player.WHITE, 5);

            System.out.println("Welcome to the Tak tutorial. Here you will learn the basics for how to play the game of Tak.");
            System.out.println("Tak is an abstract strategy game based originally referenced in the Kingkiller Chronicles series.");
            System.out.println("The goal of the game is to build a road from one side of the board to the other.");
            System.out.println();
            System.out.println("There are several board sizes, detailed here:");
            System.out.println();
            System.out.println("-----------------------------");
            System.out.println("| Size | Stones | Capstones |");
            System.out.println("| 3x3  | 10     | 0         |");
            System.out.println("| 4x4  | 15     | 0         |");
            System.out.println("| 5x5  | 21     | 1         |");
            System.out.println("| 6x6  | 30     | 1         |");
            System.out.println("| 8x6  | 50     | 2         |");
            System.out.println("-----------------------------");
            System.out.println();
            System.out.println("Each player begins with the specified number of pieces and an empty board.");
            System.out.println();
            testState.printBoard();
            System.out.println();
            System.out.println("For each player's first turn, they place a stone of the other player.");
            //have player place
            //explain board notation
            //explain different pieces
            //explain moves
            //explain win conditions
            //explain in tak
            //explain scoring
        }
        catch(TakEngineException e) {
            System.out.println("Something went wrong with the engine.");
        }
    }
}
