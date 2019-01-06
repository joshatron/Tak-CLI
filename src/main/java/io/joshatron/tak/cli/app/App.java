package io.joshatron.tak.cli.app;

import io.joshatron.tak.cli.app.local.LocalPlay;
import io.joshatron.tak.cli.app.server.ServerPlay;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;

public class App
{
    public static void main(String[] args) {
        try {
            ServerPlay serverPlay = null;
            LocalPlay localPlay = null;

            LineReader playReader = LineReaderBuilder.builder()
                    .terminal(TerminalBuilder.terminal())
                    .completer(new StringsCompleter("local", "server", "help", "exit"))
                    .build();

            System.out.println("---------------------");
            System.out.println("| Welcome to TakCLI |");
            System.out.println("---------------------");
            System.out.println();

            while(true) {
                String input = playReader.readLine("> ").toLowerCase().trim();

                if(input.equals("local")) {
                    if(localPlay == null) {
                        localPlay = new LocalPlay();
                    }
                    localPlay.play();
                }
                else if(input.equals("server")) {
                    if(serverPlay == null) {
                        serverPlay = new ServerPlay();
                    }
                    serverPlay.play();
                }
                else if(input.equals("help")) {
                    System.out.println("The following is a list of what you can do:");
                    System.out.println("  local- give parameters for playing one or more games.");
                    System.out.println("  server- connect to a server to play.");
                    System.out.println("  help- displays this help message.");
                    System.out.println("  exit- exits the program.");
                }
                else if(input.equals("exit")) {
                    break;
                }
                else {
                    System.out.println("Please choose a valid option. Type help to see options.");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
