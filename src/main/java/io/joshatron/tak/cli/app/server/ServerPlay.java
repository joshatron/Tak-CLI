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

            config = new ServerConfig();

            if (config.getServerUrl() == null) {
                config.setServerUrl(nullReader.readLine("What is the server url? "));
            }

            httpUtils = new HttpUtils(config.getServerUrl());
            authenticate();
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
            password = nullReader.readLine("What is your password? ");

            if(httpUtils.authenticate(username, password)) {
                break;
            }

            username = null;
        }
    }

    private String createPrompt() {
        SocialNotifications social = httpUtils.getSocialNotifications();
        GameNotifications game = httpUtils.getGameNotifications();

        return "fr:" + social.getFriendRequests() + "|um:" + social.getUnreadMessages() + "|gr:" +
                game.getGameRequests() + "|yt:" + game.getYourTurn() + "> ";
    }
}
