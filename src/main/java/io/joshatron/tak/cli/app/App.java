package io.joshatron.tak.cli.app;

import io.joshatron.tak.cli.app.commands.Action;
import io.joshatron.tak.cli.app.commands.Command;
import io.joshatron.tak.cli.app.commands.CommandInterpreter;
import io.joshatron.tak.cli.app.commands.TurnCompleter;
import io.joshatron.tak.cli.app.local.HumanPlayer;
import io.joshatron.tak.cli.app.local.LocalPlay;
import io.joshatron.tak.cli.app.local.Tutorial;
import io.joshatron.tak.cli.app.server.HttpUtils;
import io.joshatron.tak.cli.app.server.ServerConfig;
import io.joshatron.tak.cli.app.server.request.Answer;
import io.joshatron.tak.cli.app.server.response.*;
import io.joshatron.tak.engine.board.Direction;
import io.joshatron.tak.engine.board.PieceType;
import io.joshatron.tak.engine.exception.TakEngineException;
import io.joshatron.tak.engine.game.GameState;
import io.joshatron.tak.engine.game.Player;
import io.joshatron.tak.engine.turn.MoveTurn;
import io.joshatron.tak.engine.turn.PlaceTurn;
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
import java.util.Map;

public class App {

    private boolean online;
    private ArrayList<User> users;
    private CommandInterpreter commandInterpreter;
    private ServerConfig config;
    private HttpUtils httpUtils;

    public App() throws IOException {
        online = false;
        users = new ArrayList<>();
        commandInterpreter = new CommandInterpreter(users);
        config = new ServerConfig();
        httpUtils = null;

        if(config.getServerUrl() != null && !config.getServerUrl().isEmpty()) {
            httpUtils = new HttpUtils(config.getServerUrl());
            if(config.getUsername() != null && !config.getUsername().isEmpty()) {
                authenticate(config.getUsername());
            }
        }
    }

    public void run() throws IOException {
        LineReader nullReader = LineReaderBuilder.builder()
                .terminal(TerminalBuilder.terminal())
                .completer(new NullCompleter())
                .build();

        System.out.println("---------------------");
        System.out.println("| Welcome to TakCLI |");
        System.out.println("---------------------");
        System.out.println();

        while(true) {
            try {
                Command command;
                if(online) {
                    command = commandInterpreter.interpretCommand(createOnlinePrompt());
                } else {
                    command = commandInterpreter.interpretCommand("> ");

                    if (isOnlineCommand(command.getAction())) {
                        System.out.println("You can only do that action when you are online. To go online, type 'connect'.");
                        continue;
                    }
                }

                if(command == null) {
                    continue;
                }

                switch(command.getAction()) {
                    case CHANGE_PASSWORD:
                        String newPass = nullReader.readLine("New password: ", '*');
                        httpUtils.changePassword(newPass);
                        break;
                    case CHANGE_USERNAME:
                        httpUtils.changeUsername(command.getArg(0));
                        break;
                    case INCOMING_FRIEND_REQUESTS:
                        printUsers(httpUtils.getIncomingFriendRequests());
                        break;
                    case OUTGOING_FRIEND_REQUESTS:
                        printUsers(httpUtils.getOutgoingFriendRequests());
                        break;
                    case LIST_FRIENDS:
                        printUsers(httpUtils.getFriends());
                        break;
                    case LIST_BLOCKS:
                        printUsers(httpUtils.getBlocking());
                        break;
                    case REQUEST_FRIEND:
                        httpUtils.createFriendRequest(getIdFromUsername(command.getArg(0)));
                        break;
                    case CANCEL_FRIEND_REQUEST:
                        httpUtils.deleteFriendRequest(getIdFromUsername(command.getArg(0)));
                        break;
                    case ACCEPT_FRIEND_REQUEST:
                        httpUtils.respondToFriendRequest(getIdFromUsername(command.getArg(0)), Answer.ACCEPT);
                        break;
                    case DENY_FRIEND_REQUEST:
                        httpUtils.respondToFriendRequest(getIdFromUsername(command.getArg(0)), Answer.DENY);
                        break;
                    case UNFRIEND:
                        httpUtils.unfriendUser(getIdFromUsername(command.getArg(0)));
                        break;
                    case BLOCK:
                        httpUtils.blockUser(getIdFromUsername(command.getArg(0)));
                        break;
                    case UNBLOCK:
                        httpUtils.unblockUser(getIdFromUsername(command.getArg(0)));
                        break;
                    case SEND_MESSAGE:
                        httpUtils.sendMessage(getIdFromUsername(command.getArg(0)), command.getArg(1));
                        break;
                    case LIST_UNREAD:
                        getUnread();
                        break;
                    case MARK_ALL_READ:
                        httpUtils.markAllRead();
                        break;
                    case MESSAGES_FROM_USER:
                        printMessages(httpUtils.getMessagesFromUser(getIdFromUsername(command.getArg(0))));
                        httpUtils.markReadFromSender(getIdFromUsername(command.getArg(0)));
                        break;
                    case INCOMING_GAME_REQUESTS:
                        printGameRequests(httpUtils.getIncomingGameRequests());
                        break;
                    case OUTGOING_GAME_REQUESTS:
                        printGameRequests(httpUtils.getOutgoingGameRequests());
                        break;
                    case REQUEST_GAME:
                        httpUtils.requestGame(new RequestInfo(httpUtils.getUsername(), getIdFromUsername(command.getArg(0)),
                                Player.valueOf(command.getArg(2).toUpperCase()), Player.valueOf(command.getArg(3).toUpperCase()),
                                Integer.parseInt(command.getArg(1))));
                        break;
                    case CANCEL_GAME_REQUEST:
                        httpUtils.cancelGameRequest(getIdFromUsername(command.getArg(0)));
                        break;
                    case ACCEPT_GAME_REQUEST:
                        httpUtils.respondToGameRequest(getIdFromUsername(command.getArg(0)), Answer.ACCEPT);
                        break;
                    case DENY_GAME_REQUEST:
                        httpUtils.respondToGameRequest(getIdFromUsername(command.getArg(0)), Answer.DENY);
                        break;
                    case GET_RANDOM_GAME_REQUEST_SIZE:
                        int size = httpUtils.getRandomGameSize();
                        if(size != 0) {
                            System.out.println("Random game request size: " + size);
                        }
                        break;
                    case CREATE_RANDOM_GAME_REQUEST:
                        httpUtils.createRandomGameRequest(Integer.parseInt(command.getArg(0)));
                        break;
                    case DELETE_RANDOM_GAME_REQUEST:
                        httpUtils.cancelRandomGameRequest();
                        break;
                    case GET_OPEN_GAMES:
                        printGameInfos(httpUtils.getOpenGames());
                        break;
                    case GET_GAMES_MY_TURN:
                        printGameInfos(httpUtils.getYourTurnGames());
                        break;
                    case GET_GAME:
                        printGameBoard(httpUtils.getGameWithUser(getIdFromUsername(command.getArg(0))));
                        break;
                    case PLAY_TURN:
                        GameInfo info = httpUtils.getGameWithUser(getIdFromUsername(command.getArg(0)));
                        GameState state = getStateFromGameInfo(info);
                        Turn turn = new HumanPlayer().getTurn(state);
                        httpUtils.playTurn(info.getGameId(), turn.toString());
                        break;
                    case LOCAL_GAME:
                        LocalPlay.play(Integer.parseInt(command.getArg(4)), Integer.parseInt(command.getArg(0)),
                                Player.valueOf(command.getArg(1).toUpperCase()), command.getArg(2), command.getArg(3));
                        break;
                    case CONNECT_TO_SERVER:
                        connectToServer(command.getArg(0));
                        break;
                    case FORGET_SERVER:
                        config.forgetConfig();
                        httpUtils = null;
                        online = false;
                        break;
                    case HELP:
                        if(online) {
                            System.out.println(getOnlineHelp());
                        }
                        else {
                            System.out.println(getOfflineHelp());
                        }
                        break;
                    case TUTORIAL:
                        Tutorial.play();
                        break;
                    case LOGIN:
                        authenticate(command.getArg(0));
                        break;
                    case LOGOUT:
                        config.setUsername("");
                        httpUtils.logout();
                        online = false;
                        break;
                    case EXIT:
                        System.out.println("Exiting...");
                        config.exportConfig();
                        return;
                    default:
                        System.out.println("Invalid command. Type 'help' to see all options");
                }
            } catch (Exception e) {
                System.out.println("Something went wrong: " + e.getMessage());
            }
        }
    }

    private void connectToServer(String serverUrl) throws IOException {
        httpUtils = new HttpUtils(serverUrl);
        if(httpUtils.isConnected()) {
            config.setServerUrl(serverUrl);
            LineReader nullReader = LineReaderBuilder.builder()
                    .terminal(TerminalBuilder.terminal())
                    .completer(new NullCompleter())
                    .build();
            LineReader optionReader = LineReaderBuilder.builder()
                    .terminal(TerminalBuilder.terminal())
                    .completer(new StringsCompleter("authenticate", "register"))
                    .build();

            while(true) {
                String option = optionReader.readLine("Do you want to register or authenticate? ").toLowerCase().trim();
                if(option.equals("register")) {
                    register();
                    break;
                }
                else if(option.equals("authenticate")) {
                    String username = nullReader.readLine("What is your username? ");
                    authenticate(username);
                    break;
                }
                else if(option.isEmpty()) {
                    break;
                }
            }
        }
        else {
            System.out.println("Could not connect to server");
        }
    }

    private void authenticate(String username) throws IOException {
        LineReader nullReader = LineReaderBuilder.builder()
                .terminal(TerminalBuilder.terminal())
                .completer(new NullCompleter())
                .build();

        while(true) {
            String password = nullReader.readLine("What is your password for " + username + " (blank to cancel)? ", '*');

            if(password.isEmpty()) {
                return;
            }

            if(httpUtils.isAuthenticated(username, password)) {
                config.setUsername(username);
                online = true;
                initializeUsers();
                break;
            }
            System.out.println("Incorrect username or password. Please try again");
        }
    }

    private void register() throws IOException {
        LineReader nullReader = LineReaderBuilder.builder()
                .terminal(TerminalBuilder.terminal())
                .completer(new NullCompleter())
                .build();

        while(true) {
            String username = nullReader.readLine("What is your username? ");
            String password = nullReader.readLine("What is your password (blank to cancel)? ", '*');

            if(password.isEmpty()) {
                return;
            }

            if(httpUtils.register(username, password)) {
                config.setUsername(username);
                online = true;
                initializeUsers();
                break;
            }
            System.out.println("Can't register that username. Please try again");
        }
    }

    private boolean isOnlineCommand(Action action) {
        return (action != Action.LOCAL_GAME && action != Action.CONNECT_TO_SERVER && action != Action.LOGIN &&
                action != Action.HELP  && action != Action.TUTORIAL && action != Action.EXIT);
    }

    private String createOnlinePrompt() {
        SocialNotifications social = httpUtils.getSocialNotifications();
        GameNotifications game = httpUtils.getGameNotifications();

        return httpUtils.getUsername() + " fr:" + social.getFriendRequests() + "|ur:" + social.getUnreadMessages() +
                "|gr:" + game.getGameRequests() + "|yt:" + game.getYourTurn() + "> ";
    }

    private String getOnlineHelp() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("The prompt contains counts for all your notifications. To refresh, just hit enter.\n");
        stringBuilder.append("The notifications are as follows:\n");
        stringBuilder.append("  fr- friend requests\n");
        stringBuilder.append("  ur- unread messages\n");
        stringBuilder.append("  gr- game requests\n");
        stringBuilder.append("  yt- your turn for a game\n\n");
        stringBuilder.append("The following is a list of what you can do:\n  ");
        stringBuilder.append(Action.CHANGE_PASSWORD.getShorthand());
        stringBuilder.append("- change your password\n  ");
        stringBuilder.append(Action.CHANGE_USERNAME.getShorthand());
        stringBuilder.append(" {new username}- change your username\n  ");
        stringBuilder.append(Action.INCOMING_FRIEND_REQUESTS.getShorthand());
        stringBuilder.append("- get a list of incoming friend requests\n  ");
        stringBuilder.append(Action.OUTGOING_FRIEND_REQUESTS.getShorthand());
        stringBuilder.append("- get a list of outgoing friend requests\n  ");
        stringBuilder.append(Action.LIST_FRIENDS.getShorthand());
        stringBuilder.append("- get a list of all your friends\n  ");
        stringBuilder.append(Action.LIST_BLOCKS.getShorthand());
        stringBuilder.append("- get a list of all the users you are blocking\n  ");
        stringBuilder.append(Action.REQUEST_FRIEND.getShorthand());
        stringBuilder.append(" {user}- create a friend request\n  ");
        stringBuilder.append(Action.CANCEL_FRIEND_REQUEST.getShorthand());
        stringBuilder.append(" {user}- cancel a friend request\n  ");
        stringBuilder.append(Action.ACCEPT_FRIEND_REQUEST.getShorthand());
        stringBuilder.append(" {user}- accept a friend request\n  ");
        stringBuilder.append(Action.DENY_FRIEND_REQUEST.getShorthand());
        stringBuilder.append(" {user}- deny a friend request\n  ");
        stringBuilder.append(Action.UNFRIEND.getShorthand());
        stringBuilder.append(" {user}- unfriend a user\n  ");
        stringBuilder.append(Action.BLOCK.getShorthand());
        stringBuilder.append(" {user}- block a user\n  ");
        stringBuilder.append(Action.UNBLOCK.getShorthand());
        stringBuilder.append(" {user}- unblock a user\n  ");
        stringBuilder.append(Action.SEND_MESSAGE.getShorthand());
        stringBuilder.append(" {user} {message}- send a message to another user\n  ");
        stringBuilder.append(Action.LIST_UNREAD.getShorthand());
        stringBuilder.append("- get a list of all users you have unread messages for\n  ");
        stringBuilder.append(Action.MARK_ALL_READ.getShorthand());
        stringBuilder.append("- mark all your messages as read\n  ");
        stringBuilder.append(Action.MESSAGES_FROM_USER.getShorthand());
        stringBuilder.append(" {user}- see your messages with a user\n  ");
        stringBuilder.append(Action.INCOMING_GAME_REQUESTS.getShorthand());
        stringBuilder.append("- get a list of incoming game requests\n  ");
        stringBuilder.append(Action.OUTGOING_GAME_REQUESTS.getShorthand());
        stringBuilder.append("- get a list of outgoing game requests\n  ");
        stringBuilder.append(Action.REQUEST_GAME.getShorthand());
        stringBuilder.append(" {user} {board size} {your color} {color going first}- create a game request\n  ");
        stringBuilder.append(Action.CANCEL_GAME_REQUEST.getShorthand());
        stringBuilder.append(" {user}- cancel a game request\n  ");
        stringBuilder.append(Action.ACCEPT_GAME_REQUEST.getShorthand());
        stringBuilder.append(" {user}- accept a game request\n  ");
        stringBuilder.append(Action.DENY_GAME_REQUEST.getShorthand());
        stringBuilder.append(" {user}- deny a game request\n  ");
        stringBuilder.append(Action.GET_RANDOM_GAME_REQUEST_SIZE.getShorthand());
        stringBuilder.append("- check the size game of your random game request\n  ");
        stringBuilder.append(Action.CREATE_RANDOM_GAME_REQUEST.getShorthand());
        stringBuilder.append(" {board size}- create a request for a game with a random user\n  ");
        stringBuilder.append(Action.DELETE_RANDOM_GAME_REQUEST.getShorthand());
        stringBuilder.append("- cancel a random game request\n  ");
        stringBuilder.append(Action.GET_OPEN_GAMES.getShorthand());
        stringBuilder.append("- get a summary of all your open games\n  ");
        stringBuilder.append(Action.GET_GAMES_MY_TURN.getShorthand());
        stringBuilder.append("- get a summary of all your open games where it is your turn\n  ");
        stringBuilder.append(Action.GET_GAME.getShorthand());
        stringBuilder.append(" {user}- get a detailed view of a specific game\n  ");
        stringBuilder.append(Action.PLAY_TURN.getShorthand());
        stringBuilder.append(" {user}- open a detailed view of a game and make a turn\n  ");
        stringBuilder.append(Action.LOCAL_GAME.getShorthand());
        stringBuilder.append(" {board size} {color going first} {white human or ai} {black human or ai} {# games}- play a local game against either another player or an AI\n  ");
        stringBuilder.append(Action.FORGET_SERVER.getShorthand());
        stringBuilder.append("- forget configuration for the server you currently connect to\n  ");
        stringBuilder.append(Action.CONNECT_TO_SERVER.getShorthand());
        stringBuilder.append(" {server url}- connect to the server specified\n  ");
        stringBuilder.append(Action.HELP.getShorthand());
        stringBuilder.append("- display this help message\n  ");
        stringBuilder.append(Action.TUTORIAL.getShorthand());
        stringBuilder.append("- runs through a tutorial on how to play the game\n  ");
        stringBuilder.append(Action.LOGIN.getShorthand());
        stringBuilder.append(" {username}- logs into the server\n  ");
        stringBuilder.append(Action.LOGOUT.getShorthand());
        stringBuilder.append("- logs out of user and goes back to the main menu\n  ");
        stringBuilder.append(Action.EXIT.getShorthand());
        stringBuilder.append("- exits back to the main menu\n");

        return stringBuilder.toString();
    }

    private String getOfflineHelp() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("The following is a list of what you can do:\n  ");
        stringBuilder.append(Action.LOCAL_GAME.getShorthand());
        stringBuilder.append(" {board size} {color going first} {white human or ai} {black human or ai} {# games}- play a local game against either another player or an AI\n  ");
        stringBuilder.append(Action.CONNECT_TO_SERVER.getShorthand());
        stringBuilder.append(" {server url}- connect to the server specified\n  ");
        stringBuilder.append(Action.LOGIN.getShorthand());
        stringBuilder.append(" {username}- logs into the server\n  ");
        stringBuilder.append(Action.HELP.getShorthand());
        stringBuilder.append("- display this help message\n  ");
        stringBuilder.append(Action.TUTORIAL.getShorthand());
        stringBuilder.append("- runs through a tutorial on how to play the game\n  ");
        stringBuilder.append(Action.EXIT.getShorthand());
        stringBuilder.append("- exits back to the main menu\n");

        return stringBuilder.toString();
    }

    private void printUsers(User[] toPrint) {
        if(toPrint != null && toPrint.length > 0) {
            for (User user : toPrint) {
                System.out.println(user.getUsername());
                if(!users.contains(user)) {
                    users.add(user);
                }
            }
        }
    }

    private void printMessages(Message[] messages) {
        if(messages != null && messages.length > 0) {
            System.out.println("Your messages:");
            for(Message message : messages) {
                if(!message.isOpened()) {
                    System.out.print("*");
                }
                System.out.println(message.getTimestamp() + " " + getUsernameFromId(message.getSender()) + ": " + message.getMessage());
            }
        }
    }

    private void printGameRequests(RequestInfo[] requests) {
        if (requests != null && requests.length > 0) {
            for (RequestInfo request : requests) {
                System.out.print(getUsernameFromId(request.getRequester()));
                System.out.print(" (" + request.getRequesterColor().name() + "): ");
                System.out.print("size " + request.getSize() + ", ");
                System.out.println(request.getFirst().name() + " goes first");
            }
        }
    }

    private void printGameInfos(GameInfo[] infos) {
        if(infos != null && infos.length > 0) {
            System.out.println("Your games:");
            for (GameInfo info : infos) {
                if (info.getWhite().equals(getIdFromUsername(httpUtils.getUsername()))) {
                    System.out.print(getUsernameFromId(info.getBlack()) + " (BLACK): ");
                } else {
                    System.out.print(getUsernameFromId(info.getWhite()) + " (WHITE): ");
                }
                System.out.println("size- " + info.getSize() + ", first- " + info.getFirst() + ", current- " + info.getCurrent());
            }
        }
    }

    private void printGameBoard(GameInfo info) throws TakEngineException {
        if(info != null) {
            if(info.getWhite().equals(getIdFromUsername(httpUtils.getUsername()))) {
                System.out.print(getUsernameFromId(info.getBlack()) + " (BLACK): ");
            }
            else {
                System.out.print(getUsernameFromId(info.getBlack()) + " (WHITE): ");
            }
            System.out.println("size- " + info.getSize() + ", first- " + info.getFirst() + ", current- " + info.getCurrent());

            GameState state = getStateFromGameInfo(info);
            state.printBoard();
        }
    }

    private GameState getStateFromGameInfo(GameInfo info) throws TakEngineException {
        GameState state = new GameState(info.getFirst(), info.getSize(), true);
        for(String turn : info.getTurns()) {
            state.executeTurn(TurnUtils.turnFromString(turn));
        }

        return state;
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
            for(Map.Entry<String, Integer> entry : map.entrySet()) {
                if(entry.getValue() > 1) {
                    System.out.println(getUsernameFromId(entry.getKey()) + ": " + entry.getValue() + " unread messages");
                }
                else {
                    System.out.println(getUsernameFromId(entry.getKey()) + ": 1 unread message");
                }
            }
        }
        else {
            System.out.println("Could not find any unread messages");
        }
    }

    private void initializeUsers() {
        User[] ifrequests = httpUtils.getIncomingFriendRequests();
        User[] ofrequests = httpUtils.getOutgoingFriendRequests();
        User[] friends = httpUtils.getFriends();
        User[] blocks = httpUtils.getBlocking();

        for(User user : ifrequests) {
            if(!users.contains(user)) {
                users.add(user);
            }
        }
        for(User user : ofrequests) {
            if(!users.contains(user)) {
                users.add(user);
            }
        }
        for(User user : friends) {
            if(!users.contains(user)) {
                users.add(user);
            }
        }
        for(User user : blocks) {
            if(!users.contains(user)) {
                users.add(user);
            }
        }
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

    public static void main(String[] args) {
        try {
            new App().run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
