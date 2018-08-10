package io.joshatron.tak.cli.app;

import io.joshatron.tak.ai.neuralnet.FeedForwardNeuralNetwork;
import io.joshatron.tak.ai.player.SimpleNeuralPlayer;
import io.joshatron.tak.engine.game.*;
import io.joshatron.tak.cli.player.HumanPlayer;
import io.joshatron.tak.ai.player.RandomPlayer;
import io.joshatron.tak.engine.player.TakPlayer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.NullCompleter;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.TerminalBuilder;

import java.io.File;
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
                    whitePlayer = new RandomPlayer();
                    //whitePlayer = new SimpleNeuralPlayer(new FeedForwardNeuralNetwork(new File("0.05_0.005_0.001_100_1000000.json")));
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
                    blackPlayer = new RandomPlayer();
                    //blackPlayer = new SimpleNeuralPlayer(new FeedForwardNeuralNetwork(new File("3_0.01_0.01_0.0_40_10000000_master.json")));
                    break;
                } else {
                    System.out.println("Invalid input. Please enter human or ai.");
                }
            }

            GameSetResult results = Games.playGames(games, size, firstPlayer, whitePlayer, blackPlayer, new CLIHooks());

            if (results.getWhiteWins() > results.getBlackWins()) {
                System.out.println("White is the winner " + results.getWhiteWins() + ":" + results.getBlackWins());
            } else if (results.getWhiteWins() < results.getBlackWins()) {
                System.out.println("Black is the winner " + results.getBlackWins() + ":" + results.getWhiteWins());
            } else {
                System.out.println("It's a tie! Both players won the same number of games.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
