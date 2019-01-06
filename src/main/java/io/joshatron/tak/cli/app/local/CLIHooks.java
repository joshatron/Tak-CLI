package io.joshatron.tak.cli.app.local;

import io.joshatron.tak.engine.game.GameHooks;
import io.joshatron.tak.engine.game.GameResult;
import io.joshatron.tak.engine.game.GameState;
import io.joshatron.tak.engine.game.Player;
import io.joshatron.tak.engine.turn.Turn;

public class CLIHooks implements GameHooks {

    @Override
    public void beforeGame(GameState state, int game) {
        System.out.println();
        System.out.println("-----------------");
        System.out.printf("| Game %8d |\n", game + 1);
        System.out.println("-----------------");
        System.out.println();
    }

    @Override
    public void afterGame(GameState state, int game) {
        state.printBoard();
        GameResult result = state.checkForWinner();

        if(result.getWinner() == Player.WHITE) {
            switch(result.getReason()) {
                case OUT_OF_PIECES:
                    System.out.println("White wins by out of pieces!");
                    break;
                case BOARD_FULL:
                    System.out.println("White wins by board full!");
                    break;
                case PATH:
                    System.out.println("White wins by path!");
                    break;
            }
        }
        else if(result.getWinner() == Player.BLACK) {
            switch(result.getReason()) {
                case OUT_OF_PIECES:
                    System.out.println("Black wins by out of pieces!");
                    break;
                case BOARD_FULL:
                    System.out.println("Black wins by board full!");
                    break;
                case PATH:
                    System.out.println("Black wins by path!");
                    break;
            }
        }
        else {
            switch(result.getReason()) {
                case OUT_OF_PIECES:
                    System.out.println("It's a tie by out of pieces!");
                    break;
                case BOARD_FULL:
                    System.out.println("It's a tie by board full!");
                    break;
            }
        }
    }

    @Override
    public void beforeTurn(GameState state) {

    }

    @Override
    public void afterTurn(GameState state) {
        Turn last = state.getTurns().get(state.getTurns().size() - 1);
        if(!state.isWhiteTurn()) {
            System.out.println("White played: " + last.toString());
        }
        else {
            System.out.println("Black played: " + last.toString());
        }
    }
}
