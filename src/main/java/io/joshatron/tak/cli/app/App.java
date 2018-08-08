package io.joshatron.tak.cli.app;

import io.joshatron.tak.engine.game.*;
import io.joshatron.tak.cli.player.HumanPlayer;
import io.joshatron.tak.ai.player.RandomPlayer;
import io.joshatron.tak.engine.player.TakPlayer;
import io.joshatron.tak.engine.turn.Turn;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.NullCompleter;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;

public class App
{
    public static void main(String[] args) {
        try {
            LineReader nullReader = LineReaderBuilder.builder()
                    .terminal(TerminalBuilder.terminal())
                    .completer(new NullCompleter())
                    .build();
            LineReader boardReader = LineReaderBuilder.builder()
                    .terminal(TerminalBuilder.terminal())
                    .completer(new StringsCompleter("3","4","5","6","8"))
                    .build();
            LineReader whiteReader = LineReaderBuilder.builder()
                    .terminal(TerminalBuilder.terminal())
                    .completer(new StringsCompleter("white","black"))
                    .build();
            LineReader playerReader = LineReaderBuilder.builder()
                    .terminal(TerminalBuilder.terminal())
                    .completer(new StringsCompleter("human","ai"))
                    .build();
            int games;
            int size;
            Player firstPlayer;
            TakPlayer whitePlayer;
            TakPlayer blackPlayer;

            System.out.println("---------------------");
            System.out.println("| Welcome to TakCLI |");
            System.out.println("---------------------");
            System.out.println();

            while (true) {
                String input = nullReader.readLine("How many games would you like to play? ").trim();
                try {
                    games = Integer.parseInt(input);
                    break;
                } catch (Exception e) {
                    System.out.println("Invalid number. Please enter a valid number greater than 0.");
                }
            }
            while (true) {
                String input = boardReader.readLine("What size board would you like to play on? ").trim();
                try {
                    size = Integer.parseInt(input);
                    if(size == 3 || size == 4 || size == 5 || size == 6 || size == 8) {
                        break;
                    }
                    else {
                        System.out.println("Invalid number Please enter 3, 4, 5, 6, or 8.");
                    }
                } catch (Exception e) {
                    System.out.println("Invalid number Please enter 3, 4, 5, 6, or 8.");
                }
            }
            while (true) {
                String input = whiteReader.readLine("Who goes first, white or black? ").trim().toLowerCase();
                if (input.equals("white")) {
                    firstPlayer = Player.WHITE;
                    break;
                } else if (input.equals("black")) {
                    firstPlayer = Player.BLACK;
                    break;
                } else {
                    System.out.println("Invalid input. Please enter black or white.");
                }
            }
            while (true) {
                String input = playerReader.readLine("Is white a human or AI? ").trim().toLowerCase();
                if (input.equals("human")) {
                    whitePlayer = new HumanPlayer();
                    break;
                } else if (input.equals("ai")) {
                    /*try {
                        whitePlayer = new SimpleNeuralPlayer(new FeedForwardNeuralNetwork(new File("0.05_0.005_0.001_100_1000000.json")));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
                    whitePlayer = new RandomPlayer();
                    break;
                } else {
                    System.out.println("Invalid input. Please enter human or ai.");
                }
            }
            while (true) {
                String input = playerReader.readLine("Is black a human or AI? ").trim().toLowerCase();
                if (input.equals("human")) {
                    blackPlayer = new HumanPlayer();
                    break;
                } else if (input.equals("ai")) {
                    /*try {
                        blackPlayer = new SimpleNeuralPlayer(new FeedForwardNeuralNetwork(new File("0.005_0.1_0.001_50_10000000.json")));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
                    blackPlayer = new RandomPlayer();
                    break;
                } else {
                    System.out.println("Invalid input. Please enter human or ai.");
                }
            }

            int whiteWins = 0;
            int blackWins = 0;
            for (int i = 0; i < games; i++) {
                System.out.println("----------");
                System.out.println("| Game " + (i + 1) + " |");
                System.out.println("----------");
                GameResult result = playGame(firstPlayer, size, whitePlayer, blackPlayer);
                if (result.getReason() == WinReason.SURRENDER) {
                    System.exit(0);
                } else if (result.getWinner() == Player.WHITE) {
                    whiteWins++;
                } else if (result.getWinner() == Player.BLACK) {
                    blackWins++;
                }

                if(firstPlayer == Player.WHITE) {
                    firstPlayer = Player.BLACK;
                }
                else {
                    firstPlayer = Player.WHITE;
                }
            }

            if (whiteWins > blackWins) {
                System.out.println("White is the winner " + whiteWins + ":" + blackWins);
            } else if (whiteWins < blackWins) {
                System.out.println("Black is the winner " + blackWins + ":" + whiteWins);
            } else {
                System.out.println("It's a tie! Both players won the same number of games.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Plays game with the given conditions
    public static GameResult playGame(Player firstPlayer, int size, TakPlayer whitePlayer, TakPlayer blackPlayer) {
        System.out.println("Initializing game...");
        GameState state = new GameState(firstPlayer, size);


        while(!state.checkForWinner().isFinished()) {
            if(state.isWhiteTurn()) {
                Turn turn = whitePlayer.getTurn((GameState)state.clone());
                if(turn == null) {
                    System.out.println("White surrenders!");
                    return new GameResult(true, Player.BLACK, WinReason.SURRENDER);
                }
                state.executeTurn(turn);
                System.out.println("White played: " + turn.toString());
            }
            else {
                Turn turn = blackPlayer.getTurn((GameState)state.clone());
                if(turn == null) {
                    System.out.println("Black surrenders!");
                    return new GameResult(true, Player.WHITE, WinReason.SURRENDER);
                }
                state.executeTurn(turn);
                System.out.println("Black played: " + turn.toString());
            }
        }

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

        return result;
    }
}
