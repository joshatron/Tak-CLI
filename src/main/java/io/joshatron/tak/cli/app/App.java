package io.joshatron.tak.cli.app;

import io.joshatron.tak.cli.app.commands.Command;
import io.joshatron.tak.cli.app.commands.CommandInterpreter;
import io.joshatron.tak.cli.app.server.HttpUtils;
import io.joshatron.tak.cli.app.server.ServerConfig;
import io.joshatron.tak.cli.app.server.response.GameNotifications;
import io.joshatron.tak.cli.app.server.response.SocialNotifications;
import io.joshatron.tak.cli.app.server.response.User;

import java.io.IOException;
import java.lang.reflect.Array;
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
            Command command;
            if(online) {
                command = commandInterpreter.interpretCommand(createOnlinePrompt());
            }
            else {
                command = commandInterpreter.interpretCommand("> ");
            }

            switch(command.getAction()) {
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
                    break;
                case CONNECT_TO_SERVER:
                    break;
                case FORGET_SERVER:
                    break;
                case HELP:
                    break;
                case LOGOUT:
                    break;
                case EXIT:
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("Invalid command. Type 'help' to see all options");
            }
        }
    }

    private String createOnlinePrompt() {
        SocialNotifications social = httpUtils.getSocialNotifications();
        GameNotifications game = httpUtils.getGameNotifications();

        return httpUtils.getUsername() + " fr:" + social.getFriendRequests() + "|ur:" + social.getUnreadMessages() +
                "|gr:" + game.getGameRequests() + "|yt:" + game.getYourTurn() + "> ";
    }

    public static void main(String[] args) {
        try {
            new App().run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
