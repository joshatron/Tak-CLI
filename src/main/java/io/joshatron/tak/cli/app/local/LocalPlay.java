package io.joshatron.tak.cli.app.local;

import io.joshatron.tak.ai.player.AI;
import io.joshatron.tak.ai.player.AIFactory;
import io.joshatron.tak.engine.exception.TakEngineException;
import io.joshatron.tak.engine.game.GameSetResult;
import io.joshatron.tak.engine.game.Games;
import io.joshatron.tak.engine.game.Player;
import io.joshatron.tak.engine.player.TakPlayer;

public class LocalPlay {

    public static void play(int games, int size, Player firstPlayer, String whiteType, String blackType) {
        try {
            GameSetResult results = new Games(games, size, firstPlayer, createPlayer(whiteType, size, Player.WHITE, firstPlayer),
                    createPlayer(blackType, size, Player.BLACK, firstPlayer), new CLIHooks()).playGames();

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

    private static TakPlayer createPlayer(String type, int size, Player player, Player first) {
        if(type.equalsIgnoreCase("human")) {
            return new HumanPlayer();
        }
        else if(type.equalsIgnoreCase("ai")) {
            return AIFactory.createPlayer(AI.DEFENSIVE_MINIMAX, size, player, first);
        }

        return null;
    }
}
