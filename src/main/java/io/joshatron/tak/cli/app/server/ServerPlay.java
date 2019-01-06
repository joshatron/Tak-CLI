package io.joshatron.tak.cli.app.server;

import io.joshatron.tak.cli.app.server.response.GameNotifications;
import io.joshatron.tak.cli.app.server.response.SocialNotifications;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.NullCompleter;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;

public class ServerPlay {

    private ServerConfig config;
    private HttpUtils httpUtils;

    public ServerPlay() {
        try {
            LineReader nullReader = LineReaderBuilder.builder()
                    .terminal(TerminalBuilder.terminal())
                    .completer(new NullCompleter())
                    .build();
            LineReader optionReader = LineReaderBuilder.builder()
                    .terminal(TerminalBuilder.terminal())
                    .completer(new StringsCompleter("authenticate", "register"))
                    .build();

            boolean register = false;
            config = new ServerConfig();

            if (config.getServerUrl() == null) {
                config.setServerUrl(nullReader.readLine("What is the server url? "));
                while(true) {
                    String option = optionReader.readLine("Do you want to register or authenticate? ").toLowerCase().trim();
                    if(option.equals("register")) {
                        register = true;
                        break;
                    }
                    else if(option.equals("authenticate")) {
                        break;
                    }
                }
            }

            httpUtils = new HttpUtils(config.getServerUrl());
            if(register) {
                register();
            }
            else {
                authenticate();
            }
            config.setUsername(httpUtils.getUsername());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void play() {
        try {
            LineReader commandReader = LineReaderBuilder.builder()
                    .terminal(TerminalBuilder.terminal())
                    .completer(new StringsCompleter("exit"))
                    .build();

            while(true) {
                String input = commandReader.readLine(createPrompt()).toLowerCase().trim();

                if(input.equals("exit")) {
                    config.exportConfig();
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void authenticate() throws IOException {
        LineReader nullReader = LineReaderBuilder.builder()
                .terminal(TerminalBuilder.terminal())
                .completer(new NullCompleter())
                .build();

        String username = config.getUsername();
        String password;

        while(true) {
            if (username == null) {
                username = nullReader.readLine("What is your username? ");
            }
            else {
                System.out.println("Username: " + username);
            }
            password = nullReader.readLine("What is your password? ");

            if(httpUtils.authenticate(username, password)) {
                config.setUsername(username);
                break;
            }

            username = null;
        }
    }

    private void register() throws IOException {
        LineReader nullReader = LineReaderBuilder.builder()
                .terminal(TerminalBuilder.terminal())
                .completer(new NullCompleter())
                .build();

        while(true) {
            String username = nullReader.readLine("What is your username? ");
            String password = nullReader.readLine("What is your password? ");

            if(httpUtils.register(username, password)) {
                config.setUsername(username);
                break;
            }
            System.out.println("Can't register that username. Please try again");
        }
    }

    private String createPrompt() {
        SocialNotifications social = httpUtils.getSocialNotifications();
        GameNotifications game = httpUtils.getGameNotifications();

        return "fr:" + social.getFriendRequests() + "|ur:" + social.getUnreadMessages() + "|gr:" +
                game.getGameRequests() + "|yt:" + game.getYourTurn() + "> ";
    }
}
