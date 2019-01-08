package io.joshatron.tak.cli.app.server;

import io.joshatron.tak.cli.app.server.response.GameNotifications;
import io.joshatron.tak.cli.app.server.response.SocialNotifications;
import io.joshatron.tak.cli.app.server.response.User;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.NullCompleter;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.util.ArrayList;

public class ServerPlay {

    private ServerConfig config;
    private HttpUtils httpUtils;
    private ArrayList<User> users;

    public ServerPlay() {
        users = new ArrayList<>();

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
                    .completer(new StringsCompleter("exit", "logout", "help", "cpass", "cname", "ifrequests", "ofrequests",
                               "friends", "blocks", "frequest", "fcancel", "frespond", "unfriend", "block", "unblock",
                               "msend", "msearch", "igrequests", "ogrequests", "grequest", "gcancel", "grespond", "grand",
                               "grandmake", "granddel", "games", "myturn", "game", "play"))
                    .build();

            while(true) {
                String input = commandReader.readLine(createPrompt()).toLowerCase().trim();

                if(input.equals("cpass")) {
                    String newPass = nullReader.readLine("New password: ", '*');
                    if(httpUtils.changePassword(newPass)) {
                        System.out.println("Password successfully changed.");
                    }
                    else {
                        System.out.println("Could not change password.");
                    }
                }
                else if(input.equals("cname")) {
                    String newName = nullReader.readLine("New username: ");
                    if(httpUtils.changeUsername(newName)) {
                        config.setUsername(newName);
                        System.out.println("Username successfully changed.");
                    }
                    else {
                        System.out.println("Could not change username.");
                    }
                }
                else if(input.equals("ifrequests")) {
                    User[] users = httpUtils.getIncomingFriendRequests();
                    if(users != null && users.length > 0) {
                        System.out.println("Users requesting to be friends with you:");
                        for (User user : users) {
                            System.out.println(user.getUsername());
                            this.users.add(user);
                        }
                    }
                    else {
                        System.out.println("You have no incoming friend requests.");
                    }
                }
                else if(input.equals("ofrequests")) {
                    User[] users = httpUtils.getOutgoingFriendRequests();
                    if(users != null && users.length > 0) {
                        System.out.println("Users you are requesting to be friends with:");
                        for (User user : users) {
                            System.out.println(user.getUsername());
                            this.users.add(user);
                        }
                    }
                    else {
                        System.out.println("You have no outgoing friend requests.");
                    }
                }
                else if(input.equals("friends")) {
                    User[] users = httpUtils.getFriends();
                    if(users != null && users.length > 0) {
                        System.out.println("Your friends:");
                        for (User user : users) {
                            System.out.println(user.getUsername());
                            this.users.add(user);
                        }
                    }
                    else {
                        System.out.println("Could not find any friends.");
                    }
                }
                else if(input.equals("blocks")) {
                    User[] users = httpUtils.getBlocking();
                    if(users != null && users.length > 0) {
                        System.out.println("Users you have blocked:");
                        for (User user : users) {
                            System.out.println(user.getUsername());
                            this.users.add(user);
                        }
                    }
                    else {
                        System.out.println("Could not find any blocks.");
                    }
                }
                else if(input.equals("frequest")) {

                }
                else if(input.equals("fcancel")) {

                }
                else if(input.equals("frespond")) {

                }
                else if(input.equals("unfriend")) {

                }
                else if(input.equals("block")) {

                }
                else if(input.equals("unblock")) {

                }
                else if(input.equals("msend")) {

                }
                else if(input.equals("msearch")) {

                }
                else if(input.equals("igrequests")) {

                }
                else if(input.equals("ogrequests")) {

                }
                else if(input.equals("grequest")) {

                }
                else if(input.equals("gcancel")) {

                }
                else if(input.equals("grespond")) {

                }
                else if(input.equals("grand")) {

                }
                else if(input.equals("grandmake")) {

                }
                else if(input.equals("granddel")) {

                }
                else if(input.equals("games")) {

                }
                else if(input.equals("myturn")) {

                }
                else if(input.equals("game")) {

                }
                else if(input.equals("play")) {

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
                    System.out.println("  cpass- change your password");
                    System.out.println("  cname- change your username");
                    System.out.println("  ifrequests- get a list of incoming friend requests");
                    System.out.println("  ofrequests- get a list of outgoing friend requests");
                    System.out.println("  friends- get a list of all your friends");
                    System.out.println("  blocks- get a list of all the users you are blocking");
                    System.out.println("  frequest- create a friend request");
                    System.out.println("  fcancel- cancel a friend request");
                    System.out.println("  frespond- respond to a friend request");
                    System.out.println("  unfriend- unfriend a user");
                    System.out.println("  block- block a user");
                    System.out.println("  unblock- unblock a user");
                    System.out.println("  msend- send a message to another user");
                    System.out.println("  msearch- search your messages");
                    System.out.println("  igrequests- get a list of incoming game requests");
                    System.out.println("  ogrequests- get a list of outgoing game requests");
                    System.out.println("  grequest- create a game request");
                    System.out.println("  gcancel- cancel a game request");
                    System.out.println("  grespond- respond to a game request");
                    System.out.println("  grand- check the size game of your random game request");
                    System.out.println("  grandmake- create a request for a game with a random user");
                    System.out.println("  granddel- cancel a random game request");
                    System.out.println("  games- get a summary of all your open games");
                    System.out.println("  myturn- get a summary of all your open games where it is your turn");
                    System.out.println("  game- get a detailed view of a specific game");
                    System.out.println("  play- open a detailed view of a game and make a turn");
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

    private String getUsernameFromId(String id) {
        for(User user : users) {
            if(user.getUserId().equalsIgnoreCase(id)) {
                return user.getUsername();
            }
        }

        User user = httpUtils.getUserFromUserId(id);
        if(user != null) {
            users.add(user);
            return user.getUsername();
        }

        return null;
    }

    private String getIdFromUsername(String username) {
        for(User user : users) {
            if(user.getUsername().equalsIgnoreCase(username)) {
                return user.getUsername();
            }
        }

        User user = httpUtils.getUserFromUsername(username);
        if(user != null) {
            users.add(user);
            return user.getUserId();
        }

        return null;
    }
}
