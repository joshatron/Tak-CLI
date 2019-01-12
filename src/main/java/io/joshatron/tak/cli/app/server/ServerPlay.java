package io.joshatron.tak.cli.app.server;

import io.joshatron.tak.cli.app.server.request.Answer;
import io.joshatron.tak.cli.app.server.response.*;
import io.joshatron.tak.engine.game.GameState;
import io.joshatron.tak.engine.game.Player;
import io.joshatron.tak.engine.turn.Turn;
import io.joshatron.tak.engine.turn.TurnUtils;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.NullCompleter;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ServerPlay {

    private ServerConfig config;
    private HttpUtils httpUtils;
    private ArrayList<User> users;

    public ServerPlay() {
        login();
    }

    private void login() {
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
            if(config.getUsername() == null) {
                login();
            }

            LineReader nullReader = LineReaderBuilder.builder()
                    .terminal(TerminalBuilder.terminal())
                    .completer(new NullCompleter())
                    .build();
            LineReader commandReader = LineReaderBuilder.builder()
                    .terminal(TerminalBuilder.terminal())
                    .completer(new StringsCompleter("exit", "logout", "help", "cpass", "cname", "ifrequests", "ofrequests",
                               "friends", "blocks", "frequest", "fcancel", "faccept", "fdeny", "unfriend", "block", "unblock",
                               "msend", "munread", "mread", "muser", "igrequests", "ogrequests", "grequest", "gcancel",
                               "gaccept", "gdeny", "grand", "grandmake", "granddel", "games", "myturn", "game", "play"))
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
                    String user = getUser();
                    if(httpUtils.createFriendRequest(user)) {
                        System.out.println("Friend request sent");
                    }
                    else {
                        System.out.println("Could not create the friend request");
                    }
                }
                else if(input.equals("fcancel")) {
                    String user = getUser();
                    if(httpUtils.deleteFriendRequest(user)) {
                        System.out.println("Friend request deleted");
                    }
                    else {
                        System.out.println("Could not delete the friend request");
                    }
                }
                else if(input.equals("faccept")) {
                    String user = getUser();
                    if(httpUtils.respondToFriendRequest(user, Answer.ACCEPT)) {
                        System.out.println("You are now friends");
                    }
                    else {
                        System.out.println("Could not respond to request");
                    }
                }
                else if(input.equals("fdeny")) {
                    String user = getUser();
                    if(httpUtils.respondToFriendRequest(user, Answer.DENY)) {
                        System.out.println("You are now friends");
                    }
                    else {
                        System.out.println("Could not respond to request");
                    }
                }
                else if(input.equals("unfriend")) {
                    String user = getUser();
                    if(httpUtils.unfriendUser(user)) {
                        System.out.println("You no longer friends");
                    }
                    else {
                        System.out.println("Could not unfriend that user");
                    }
                }
                else if(input.equals("block")) {
                    String user = getUser();
                    if(httpUtils.blockUser(user)) {
                        System.out.println("You have successfully blocked that user");
                    }
                    else {
                        System.out.println("Could not block that user");
                    }
                }
                else if(input.equals("unblock")) {
                    String user = getUser();
                    if(httpUtils.unblockUser(user)) {
                        System.out.println("You have successfully unblocked that user");
                    }
                    else {
                        System.out.println("Could not unblock that user");
                    }
                }
                else if(input.equals("msend")) {
                    String user = getUser();
                    String message = getText("What is your message? ");
                    if(httpUtils.sendMessage(user, message)) {
                        System.out.println("Your message was sent");
                    }
                    else {
                        System.out.println("Your message could not be sent");
                    }
                }
                else if(input.equals("munread")) {
                    getUnread();
                }
                else if(input.equals("mread")) {
                    if(httpUtils.markAllRead()) {
                        System.out.println("Your messages have been marked read");
                    }
                    else {
                        System.out.println("Your messages could not be marked read");
                    }
                }
                else if(input.equals("muser")) {
                    String user = getUser();
                    Message[] messages = httpUtils.getMessagesFromUser(user);
                    if(messages != null && messages.length > 0) {
                        System.out.println("Your messages with " + getUsernameFromId(user));
                        for(Message message : messages) {
                            if(!message.isOpened()) {
                                System.out.print("*");
                            }
                            System.out.println(message.getTimestamp() + " " + getUsernameFromId(message.getSender()) + ": " + message.getMessage());
                        }
                        httpUtils.markReadFromSender(user);
                    }
                    else {
                        System.out.println("Could not find any messages with that user");
                    }
                }
                else if(input.equals("igrequests")) {
                    RequestInfo[] requests = httpUtils.getIncomingGameRequests();
                    if(requests != null && requests.length > 0) {
                        System.out.println("Users requesting a game with you:");
                        for (RequestInfo request : requests) {
                            System.out.print(getUsernameFromId(request.getRequester()));
                            System.out.print(" (" + request.getRequesterColor().name() + "): ");
                            System.out.println(request.getFirst().name() + " goes first");
                        }
                    }
                    else {
                        System.out.println("You have no incoming game requests.");
                    }
                }
                else if(input.equals("ogrequests")) {
                    RequestInfo[] requests = httpUtils.getOutgoingGameRequests();
                    if(requests != null && requests.length > 0) {
                        System.out.println("Users you are requesting a game with:");
                        for (RequestInfo request : requests) {
                            System.out.print(getUsernameFromId(request.getAcceptor()));
                            System.out.print(" (" + request.getRequesterColor().other().name() + "): ");
                            System.out.println(request.getFirst().name() + " goes first");
                        }
                    }
                    else {
                        System.out.println("You have no outgoing game requests.");
                    }
                }
                else if(input.equals("grequest")) {
                    RequestInfo requestInfo = getRequest();
                    if(httpUtils.requestGame(requestInfo)) {
                        System.out.println("Your request was sent");
                    }
                    else {
                        System.out.println("Your request could not be sent");
                    }
                }
                else if(input.equals("gcancel")) {
                    String user = getUser();
                    if(httpUtils.cancelGameRequest(user)) {
                        System.out.println("You have successfully cancelled the game");
                    }
                    else {
                        System.out.println("Could not cancel the game request");
                    }
                }
                else if(input.equals("gaccept")) {
                    String user = getUser();
                    if(httpUtils.respondToGameRequest(user, Answer.ACCEPT)) {
                        System.out.println("You have successfully responded to the game");
                    }
                    else {
                        System.out.println("Could not respond to the game request");
                    }
                }
                else if(input.equals("gdeny")) {
                    String user = getUser();
                    if(httpUtils.respondToGameRequest(user, Answer.DENY)) {
                        System.out.println("You have successfully responded to the game");
                    }
                    else {
                        System.out.println("Could not respond to the game request");
                    }
                }
                else if(input.equals("grand")) {
                    int size = httpUtils.getRandomGameSize();
                    if(size != 0) {
                        System.out.println("Random game request size: " + size);
                    }
                    else {
                        System.out.println("Could not retrieve you random game request size");
                    }
                }
                else if(input.equals("grandmake")) {
                    int size = getBoardSize();
                    if(httpUtils.createRandomGameRequest(size)) {
                        System.out.println("Random game request created");
                    }
                    else {
                        System.out.println("Could not make a random game request");
                    }
                }
                else if(input.equals("granddel")) {
                    if(httpUtils.cancelRandomGameRequest()) {
                        System.out.println("Cancelled random game request");
                    }
                    else {
                        System.out.println("Could not cancel random game request");
                    }
                }
                else if(input.equals("games")) {
                    GameInfo[] games = httpUtils.getOpenGames();
                    if(games != null && games.length > 0) {
                        printGameInfos(games);
                    }
                    else {
                        System.out.println("Could not find any games");
                    }
                }
                else if(input.equals("myturn")) {
                    GameInfo[] games = httpUtils.getYourTurnGames();
                    if(games != null && games.length > 0) {
                        printGameInfos(games);
                    }
                    else {
                        System.out.println("Could not find any games");
                    }
                }
                else if(input.equals("game")) {
                    String user = getUser();
                    GameInfo info = httpUtils.getGameWithUser(user);
                    if(info != null) {
                        if(info.getWhite().equals(getIdFromUsername(httpUtils.getUsername()))) {
                            System.out.print(getUsernameFromId(info.getBlack()) + " (BLACK): ");
                        }
                        else {
                            System.out.print(getUsernameFromId(info.getBlack()) + " (WHITE): ");
                        }
                        System.out.print(info.getSize() + " first: " + info.getFirst() + ", current: " + info.getCurrent());

                        GameState state = getStateFromGameInfo(info);
                        state.printBoard();
                    }
                    else {
                        System.out.println("Could not find the game");
                    }
                }
                else if(input.equals("play")) {
                    String user = getUser();
                    GameInfo info = httpUtils.getGameWithUser(user);
                    if(info != null) {
                        if(info.getWhite().equals(getIdFromUsername(httpUtils.getUsername()))) {
                            System.out.print(getUsernameFromId(info.getBlack()) + " (BLACK): ");
                        }
                        else {
                            System.out.print(getUsernameFromId(info.getBlack()) + " (WHITE): ");
                        }
                        System.out.println(info.getSize() + " first: " + info.getFirst() + ", current: " + info.getCurrent());

                        GameState state = getStateFromGameInfo(info);
                        state.printBoard();
                        while(true) {
                            String turn = nullReader.readLine("What turn do you want to make? ");
                            if(state.isLegalTurn(TurnUtils.turnFromString(turn))) {
                                httpUtils.playTurn(info.getGameId(), turn);
                                break;
                            }
                            else if(turn.equalsIgnoreCase("exit")) {
                                System.out.println("Canceling turn play");
                                break;
                            }
                            else {
                                System.out.println("Invalid move");
                            }
                        }
                    }
                    else {
                        System.out.println("Could not find the game");
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
                    System.out.println("  cpass- change your password");
                    System.out.println("  cname- change your username");
                    System.out.println("  ifrequests- get a list of incoming friend requests");
                    System.out.println("  ofrequests- get a list of outgoing friend requests");
                    System.out.println("  friends- get a list of all your friends");
                    System.out.println("  blocks- get a list of all the users you are blocking");
                    System.out.println("  frequest- create a friend request");
                    System.out.println("  fcancel- cancel a friend request");
                    System.out.println("  faccept- accept a friend request");
                    System.out.println("  fdeny- deny a friend request");
                    System.out.println("  unfriend- unfriend a user");
                    System.out.println("  block- block a user");
                    System.out.println("  unblock- unblock a user");
                    System.out.println("  msend- send a message to another user");
                    System.out.println("  munread- get a list of all users you have unread messages for");
                    System.out.println("  mread- mark all your messages as read");
                    System.out.println("  muser- see your messages with a user");
                    System.out.println("  igrequests- get a list of incoming game requests");
                    System.out.println("  ogrequests- get a list of outgoing game requests");
                    System.out.println("  grequest- create a game request");
                    System.out.println("  gcancel- cancel a game request");
                    System.out.println("  gaccept- accept a game request");
                    System.out.println("  gdeny- deny a game request");
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
                else {
                    System.out.println("Invalid command. If you are unsure what you can do, type 'help'");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getUser() throws IOException {
        LineReader usernameReader = LineReaderBuilder.builder()
                .terminal(TerminalBuilder.terminal())
                .completer(new UsernameCompleter(users))
                .build();

        while(true) {
            String username = usernameReader.readLine("What is the username? ").trim();

            String id = getIdFromUsername(username);

            if(id == null) {
                System.out.println("Could not find the user.");
            }
            else {
                return id;
            }
        }
    }

    private String getText(String prompt) throws IOException {
        LineReader nullReader = LineReaderBuilder.builder()
                .terminal(TerminalBuilder.terminal())
                .completer(new NullCompleter())
                .build();

        return nullReader.readLine(prompt);
    }

    private int getBoardSize() throws IOException {
        LineReader sizeReader = LineReaderBuilder.builder()
                .terminal(TerminalBuilder.terminal())
                .completer(new StringsCompleter("3", "4", "5", "6", "8"))
                .build();

        int size;
        while(true) {
            try {
                size = Integer.parseInt(sizeReader.readLine("What size game do you want to play? ").trim());
                if(size != 3 && size != 4 && size != 5 && size != 6 && size != 8) {
                    System.out.println("Please choose a valid size.");
                }
                else {
                    break;
                }
            } catch (Exception e) {
                System.out.println("Please choose a valid size.");
            }
        }

        return size;
    }

    private RequestInfo getRequest() throws IOException {
        LineReader playerReader = LineReaderBuilder.builder()
                .terminal(TerminalBuilder.terminal())
                .completer(new StringsCompleter("white", "black"))
                .build();

        String user = getUser();
        int size = getBoardSize();
        Player requesterColor;
        Player first;

        while(true) {
            try {
                requesterColor = Player.valueOf(playerReader.readLine("What color do you want to be? ").trim().toUpperCase());
                break;
            } catch (Exception e) {
                System.out.println("Please choose either black or white.");
            }
        }
        while(true) {
            try {
                first = Player.valueOf(playerReader.readLine("What color should go first? ").trim().toUpperCase());
                break;
            } catch (Exception e) {
                System.out.println("Please choose either black or white.");
            }
        }

        return new RequestInfo(httpUtils.getUsername(), user, requesterColor, first, size);
    }

    private void getUnread() {
        Message[] messages = httpUtils.getUnreadMessages();
        if(messages != null && messages.length > 0) {
            HashMap<String, Integer> map = new HashMap<>();
            for(Message message : messages) {
                if(map.containsKey(message.getSender())) {
                    map.put(message.getSender(), map.get(message.getSender()) + 1);
                }
                else {
                    map.put(message.getSender(), 1);
                }
            }

            System.out.println("Users who have sent you messages:");
            for(String key : map.keySet()) {
                if(map.get(key) > 1) {
                    System.out.println(getUsernameFromId(key) + ": " + map.get(key) + " unread messages");
                }
                else {
                    System.out.println(getUsernameFromId(key) + ": 1 unread message");
                }
            }
        }
        else {
            System.out.println("Could not find any unread messages");
        }
    }

    private GameState getStateFromGameInfo(GameInfo info) {
        GameState state = new GameState(info.getFirst(), info.getSize());
        for(String turn : info.getTurns()) {
            state.executeTurn(TurnUtils.turnFromString(turn));
        }

        return state;
    }

    private void printGameInfos(GameInfo[] infos) {
        System.out.println("Your games:");
        for(GameInfo info : infos) {
            if(info.getWhite().equals(getIdFromUsername(httpUtils.getUsername()))) {
                System.out.print(getUsernameFromId(info.getBlack()) + " (BLACK): ");
            }
            else {
                System.out.print(getUsernameFromId(info.getWhite()) + " (WHITE): ");
            }
            System.out.println(info.getSize() + " first: " + info.getFirst() + ", current: " + info.getCurrent());
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
                return user.getUserId();
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
