package io.joshatron.player;

import io.joshatron.engine.GameResult;
import io.joshatron.engine.GameState;
import io.joshatron.engine.Player;
import io.joshatron.engine.Turn;
import io.joshatron.neuralnet.FeedForwardNeuralNetwork;
import io.joshatron.neuralnet.NetUtils;

public class MiniMaxPlayer implements TakPlayer {

    FeedForwardNeuralNetwork net;

    public MiniMaxPlayer(FeedForwardNeuralNetwork net) {
        this.net = net;
    }

    @Override
    public Turn getTurn(GameState state) {
        int depth = 2;

        double alpha = -9999;
        double beta = 9999;

        double best = -9999;
        Turn bestTurn = null;
        for(Turn turn : state.getPossibleTurns()) {
            state.executeTurn(turn);
            double value = getTurnValue(state, false, !state.isWhiteTurn(), depth, alpha, beta);

            if(value > best) {
                best = value;
                bestTurn = turn;
            }

            state.undoTurn();
        }

        return bestTurn;
    }

    private double getTurnValue(GameState state, boolean max, boolean white, int depth, double alpha, double beta) {
        GameResult result = state.checkForWinner();
        if(result.isFinished() && result.getWinner() == Player.WHITE) {
            if(white) {
                return 999;
            }
            else {
                return -999;
            }
        }
        else if(result.isFinished() && result.getWinner() == Player.BLACK) {
            if(white) {
                return -999;
            }
            else {
                return 999;
            }
        }

        if(depth == 0) {
            double[] out = net.compute(NetUtils.getInputs(state, state.isWhiteTurn()));

            if(state.isWhiteTurn()) {
                return out[0];
            }
            else {
                return out[1];
            }
        }

        double extreme;
        if(max) {
            extreme = -9999;
        }
        else {
            extreme = 9999;
        }

        for(Turn turn : state.getPossibleTurns()) {
            state.executeTurn(turn);
            double value = getTurnValue(state, !max, white, depth - 1, alpha, beta);
            if(max) {
                if(value > extreme) {
                    extreme = value;
                    alpha = value;
                }
            }
            else {
                if(value < extreme) {
                    extreme = value;
                    beta = value;
                }
            }

            state.undoTurn();

            if(alpha >= beta) {
                return value;
            }
        }

        return extreme;
    }
}
