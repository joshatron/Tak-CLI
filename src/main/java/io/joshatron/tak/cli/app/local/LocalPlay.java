package io.joshatron.tak.cli.app.local;

import io.joshatron.tak.ai.player.DefensiveEvaluator;
import io.joshatron.tak.ai.player.MiniMaxPlayer;
import io.joshatron.tak.engine.exception.TakEngineException;
import io.joshatron.tak.engine.game.GameSetResult;
import io.joshatron.tak.engine.game.Games;
import io.joshatron.tak.engine.game.Player;
import io.joshatron.tak.engine.player.TakPlayer;

public class LocalPlay {

    public static void play(int games, int size, Player firstPlayer, String whiteType, String blackType) {
        try {
            GameSetResult results = new Games(games, size, firstPlayer, createPlayer(whiteType, size), createPlayer(blackType, size), new CLIHooks()).playGames();

            if (results.getWhiteWins() > results.getBlackWins()) {
                System.out.println("White is the winner " + results.getWhiteWins() + ":" + results.getBlackWins());
            } else if (results.getWhiteWins() < results.getBlackWins()) {
                System.out.println("Black is the winner " + results.getBlackWins() + ":" + results.getWhiteWins());
            } else {
                System.out.println("It's a tie! Both players won the same number of games.");
            }
        } catch (TakEngineException e) {
            System.out.println(e.getCode());
            e.printStackTrace();
        }
    }

    private static TakPlayer createPlayer(String type, int size) {
        if(type.equalsIgnoreCase("human")) {
            return new HumanPlayer();
        }
        else if(type.equalsIgnoreCase("ai")) {
            return new MiniMaxPlayer(new DefensiveEvaluator(), depthFromBoardSize(size));
        }

        return null;
    }

    private static int depthFromBoardSize(int size) {
        switch (size) {
            case 3:
                return 5;
            case 4:
                return 4;
            case 5:
                return 3;
            case 6:
                return 3;
            case 8:
                return 2;
            default:
                return 2;
        }
    }
}
