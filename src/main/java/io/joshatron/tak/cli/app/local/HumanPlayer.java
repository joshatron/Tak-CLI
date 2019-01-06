package io.joshatron.tak.cli.app.local;

import io.joshatron.tak.engine.board.BoardLocation;
import io.joshatron.tak.engine.board.Direction;
import io.joshatron.tak.engine.board.PieceType;
import io.joshatron.tak.engine.game.*;
import io.joshatron.tak.engine.player.TakPlayer;
import io.joshatron.tak.engine.turn.MoveTurn;
import io.joshatron.tak.engine.turn.PlaceTurn;
import io.joshatron.tak.engine.turn.Turn;

import java.util.Scanner;

public class HumanPlayer implements TakPlayer
{
    public Turn getTurn(GameState state) {
        Scanner reader = new Scanner(System.in);

        state.printBoard();
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
                Turn turn = turnFromString(input);
                if (turn != null && state.isLegalTurn(turn)) {
                    return turn;
                } else {
                    System.out.println("Invalid move. If you need help, type help. To surrender, type exit.");
                }
            }
        }
    }

    private static Turn turnFromString(String str) {
        str = str.toLowerCase();
        if(str.charAt(0) == 'p') {
            PieceType type = null;
            switch(str.charAt(1)) {
                case 's':
                    type = PieceType.STONE;
                    break;
                case 'w':
                    type = PieceType.WALL;
                    break;
                case 'c':
                    type = PieceType.CAPSTONE;
                    break;
            }

            int x = xToNum(str.charAt(3));
            int y = charToNum(str.charAt(4)) - 1;

            if(type != null && x != -1 && y != -1) {
                PlaceTurn turn = new PlaceTurn(new BoardLocation(x, y), type);
                return turn;
            }
        }
        else if(str.charAt(0) == 'm') {
            Direction dir = null;
            switch(str.charAt(1)) {
                case 'n':
                    dir = Direction.NORTH;
                    break;
                case 's':
                    dir = Direction.SOUTH;
                    break;
                case 'e':
                    dir = Direction.EAST;
                    break;
                case 'w':
                    dir = Direction.WEST;
                    break;
            }

            int x = xToNum(str.charAt(3));
            int y = charToNum(str.charAt(4)) - 1;

            int pickUp = charToNum(str.charAt(7));

            int spots = (str.length() - 8) / 2;
            int[] drop = new int[spots];
            for(int i = 0; i < spots; i++) {
                drop[i] = charToNum(str.charAt(8 + (2 * i) + 1));
            }

            if(dir != null && x != -1 && y != -1 && pickUp != -1) {
                MoveTurn turn = new MoveTurn(new BoardLocation(x, y), pickUp, dir, drop);
                return turn;
            }
        }

        return null;
    }

    private static int xToNum(char c) {
        switch(c) {
            case 'a':
                return 0;
            case 'b':
                return 1;
            case 'c':
                return 2;
            case 'd':
                return 3;
            case 'e':
                return 4;
            case 'f':
                return 5;
            case 'g':
                return 6;
            case 'h':
                return 7;
        }

        return -1;
    }

    private static int charToNum(char c) {
        switch(c) {
            case '0':
                return 0;
            case '1':
                return 1;
            case '2':
                return 2;
            case '3':
                return 3;
            case '4':
                return 4;
            case '5':
                return 5;
            case '6':
                return 6;
            case '7':
                return 7;
            case '8':
                return 8;
            case '9':
                return 9;
        }

        return -1;
    }
}
