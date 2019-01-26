package io.joshatron.tak.cli.app;

import io.joshatron.tak.cli.app.commands.Action;
import io.joshatron.tak.cli.app.commands.Command;
import io.joshatron.tak.cli.app.commands.CommandInterpreter;
import io.joshatron.tak.cli.app.local.LocalPlay;
import io.joshatron.tak.cli.app.server.HttpUtils;
import io.joshatron.tak.cli.app.server.ServerConfig;
import io.joshatron.tak.cli.app.server.response.GameNotifications;
import io.joshatron.tak.cli.app.server.response.SocialNotifications;
import io.joshatron.tak.cli.app.server.response.User;
import io.joshatron.tak.engine.game.Player;

import java.io.IOException;
import java.util.ArrayList;

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

        if(config.getServerUrl() != null) {
            online = true;
            httpUtils = new HttpUtils(config.getServerUrl());
        }
    }

    public void run() {

        System.out.println("---------------------");
        System.out.println("| Welcome to TakCLI |");
        System.out.println("---------------------");
        System.out.println();

        while(true) {
            try {
                Command command;
                if (online) {
                    command = commandInterpreter.interpretCommand(createOnlinePrompt());
                } else {
                    command = commandInterpreter.interpretCommand("> ");

                    if (isOnlineCommand(command.getAction())) {
                        System.out.println("You can only do that action when you are online. To go online, type 'connect'.");
                        continue;
                    }
                }

                switch (command.getAction()) {
                    case CHANGE_PASSWORD:
                        break;
                    case CHANGE_USERNAME:
                        break;
                    case INCOMING_FRIEND_REQUESTS:
                        break;
                    case OUTGOING_FRIEND_REQUESTS:
                        break;
                    case LIST_FRIENDS:
                        break;
                    case LIST_BLOCKS:
                        break;
                    case REQUEST_FRIEND:
                        break;
                    case CANCEL_FRIEND_REQUEST:
                        break;
                    case ACCEPT_FRIEND_REQUEST:
                        break;
                    case DENY_FRIEND_REQUEST:
                        break;
                    case UNFRIEND:
                        break;
                    case BLOCK:
                        break;
                    case UNBLOCK:
                        break;
                    case SEND_MESSAGE:
                        break;
                    case LIST_UNREAD:
                        break;
                    case MARK_ALL_READ:
                        break;
                    case MESSAGES_FROM_USER:
                        break;
                    case INCOMING_GAME_REQUESTS:
                        break;
                    case OUTGOING_GAME_REQUESTS:
                        break;
                    case REQUEST_GAME:
                        break;
                    case CANCEL_GAME_REQUEST:
                        break;
                    case ACCEPT_GAME_REQUEST:
                        break;
                    case DENY_GAME_REQUEST:
                        break;
                    case GET_RANDOM_GAME_REQUEST_SIZE:
                        break;
                    case CREATE_RANDOM_GAME_REQUEST:
                        break;
                    case DELETE_RANDOM_GAME_REQUEST:
                        break;
                    case GET_OPEN_GAMES:
                        break;
                    case GET_GAMES_MY_TURN:
                        break;
                    case GET_GAME:
                        break;
                    case PLAY_TURN:
                        break;
                    case LOCAL_GAME:
                        LocalPlay.play(Integer.parseInt(command.getArg(4)), Integer.parseInt(command.getArg(0)),
                                Player.valueOf(command.getArg(1).toUpperCase()), command.getArg(2), command.getArg(3));
                        break;
                    case CONNECT_TO_SERVER:
                        break;
                    case FORGET_SERVER:
                        break;
                    case HELP:
                        if(online) {
                            System.out.println(getOnlineHelp());
                        }
                        else {
                            System.out.println(getOfflineHelp());
                        }
                        break;
                    case LOGOUT:
                        break;
                    case EXIT:
                        System.out.println("Exiting...");
                        return;
                    default:
                        System.out.println("Invalid command. Type 'help' to see all options");
                }
            } catch (Exception e) {
                System.out.println("Something went wrong: " + e.getMessage());
            }
        }
    }

    private boolean isOnlineCommand(Action action) {
        return (action != Action.LOCAL_GAME && action != Action.CONNECT_TO_SERVER &&
                action != Action.HELP && action != Action.EXIT);
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
        stringBuilder.append(" {new password}- change your password\n  ");
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
        stringBuilder.append(Action.HELP.getShorthand());
        stringBuilder.append("- display this help message\n  ");
        stringBuilder.append(Action.EXIT.getShorthand());
        stringBuilder.append("- exits back to the main menu\n");

        return stringBuilder.toString();
    }

    public static void main(String[] args) {
        try {
            new App().run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
