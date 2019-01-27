package io.joshatron.tak.cli.app.local;

import io.joshatron.tak.engine.game.GameState;
import io.joshatron.tak.engine.player.TakPlayer;
import io.joshatron.tak.engine.turn.Turn;
import io.joshatron.tak.engine.turn.TurnUtils;

import java.util.Scanner;

public class HumanPlayer implements TakPlayer
{
    public Turn getTurn(GameState state) {
        Scanner reader = new Scanner(System.in);

        state.printBoard();
        if(state.inTak()) {
            System.out.println("You are in tak");
        }
        while(true) {
            if (state.isWhiteTurn()) {
                System.out.print("White move: ");
            } else {
                System.out.print("Black move: ");
            }
            String input = reader.nextLine().toLowerCase();

            if (input.equals("exit") || input.equals("quit")) {
                return null;
            } else if (input.equals("help")) {
                System.out.println("To place, the format is:");
                System.out.println("\n    p[swc] [location]\n");
                System.out.println("For [swc], s is stone, w is wall, and c is capstone.");
                System.out.println("The location is in the format letter, then number.");
                System.out.println("For example, the top left corner is A1. Case doesn't matter.");
                System.out.println("An example place move would be 'ps C2'.");
                System.out.println("This would place a regular stone at position C2.\n");
                System.out.println("To move, the format is:");
                System.out.println("\n    m[nsew] [location] g[pickup] [place first to last]\n");
                System.out.println("For [nsew], n is north(up), s is south(down), e is east(right), and w is west(left)");
                System.out.println("The location is the spot to pick up from. It is the same format as in place.");
                System.out.println("Pickup is the number of pieces to pick up from the start location");
                System.out.println("Place first to last is how many to place at each spot, separate by spaces.");
                System.out.println("An example move would be 'mn A3 g3 2 1'.");
                System.out.println("This would pick up 3 pieces from A3, place 2 on A2, and one on A1.\n");
                System.out.println("To quit the game, type 'exit'");
            } else {
                Turn turn = TurnUtils.turnFromString(input);
                if (turn != null && state.isLegalTurn(turn)) {
                    return turn;
                } else {
                    System.out.println("Invalid move. If you need help, type help. To surrender, type exit.");
                }
            }
        }
    }
}
