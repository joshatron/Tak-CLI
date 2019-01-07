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
            LineReader nullReader = LineReaderBuilder.builder()
                    .terminal(TerminalBuilder.terminal())
                    .completer(new NullCompleter())
                    .build();
            LineReader commandReader = LineReaderBuilder.builder()
                    .terminal(TerminalBuilder.terminal())
                    .completer(new StringsCompleter("exit", "logout", "help", "changePass", "changeName"))
                    .build();

            while(true) {
                String input = commandReader.readLine(createPrompt()).toLowerCase().trim();

                if(input.equals("changepass")) {
                    String newPass = nullReader.readLine("New password: ", '*');
                    if(httpUtils.changePassword(newPass)) {
                        System.out.println("Password successfully changed.");
                    }
                    else {
                        System.out.println("Could not change password.");
                    }
                }
                else if(input.equals("changename")) {
                    String newName = nullReader.readLine("New username: ");
                    if(httpUtils.changeUsername(newName)) {
                        config.setUsername(newName);
                        System.out.println("Username successfully changed.");
                    }
                    else {
                        System.out.println("Could not change username.");
                    }
                }
                else if(input.equals("logout")) {
                    config.setUsername(null);
                    config.exportConfig();
                    httpUtils.logout();
                    break;
                }
                else if(input.equals("help")) {
                    System.out.println("The prompt contains counts for all your notifications. To refresh, just hit enter.");
                    System.out.println("The notifications are as follows:");
                    System.out.println("  fr- friend requests");
                    System.out.println("  ur- unread messages");
                    System.out.println("  gr- game requests");
                    System.out.println("  yt- your turn for a game");
                    System.out.println();
                    System.out.println("The following is a list of what you can do:");
                    System.out.println("  changePass- change your password");
                    System.out.println("  changeName- change your username");
                    System.out.println("  help- display this help message");
                    System.out.println("  logout- logs out of user and goes back to the main menu");
                    System.out.println("  exit- exits back to the main menu");
                }
                else if(input.equals("exit")) {
                    config.exportConfig();
                    break;
                }
                else if(input.equals("")) {
                    continue;
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
            String passwordPrompt = "What is your password(blank for different user)? ";
            if (username == null) {
                username = nullReader.readLine("What is your username? ");
                passwordPrompt = "What is your password? ";
            }
            else {
                System.out.println("Username: " + username);
            }
            password = nullReader.readLine(passwordPrompt, '*');

            if(password.equals("")) {
                username = null;
                continue;
            }

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
            String password = nullReader.readLine("What is your password? ", '*');

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

        return config.getUsername() + " fr:" + social.getFriendRequests() + "|ur:" + social.getUnreadMessages() +
               "|gr:" + game.getGameRequests() + "|yt:" + game.getYourTurn() + "> ";
    }
}
