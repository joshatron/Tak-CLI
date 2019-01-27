package io.joshatron.tak.cli.app.commands;

import io.joshatron.tak.cli.app.server.response.User;
import org.jline.reader.Completer;
import org.jline.builtins.Completers.RegexCompleter;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.NullCompleter;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandInterpreter {

    private LineReader commandReader;

    public CommandInterpreter(List<User> users) throws IOException {
        commandReader = LineReaderBuilder.builder()
                .terminal(TerminalBuilder.terminal())
                .completer(createCompleter(users))
                .build();
    }

    public Command interpretCommand(String prompt) {
        String input = commandReader.readLine(prompt).trim();
        if(input.isEmpty()) {
            return null;
        }

        Action action = Action.getAction(input.split(" ")[0].toLowerCase());
        if(action == null) {
            System.out.println("Command not found. Please type 'help' to see a list of commands");
            return null;
        }

        //remove command from input
        String[] args = null;
        if(input.length() > action.getShorthand().length()) {
            input = input.substring(action.getShorthand().length() + 1);
            if (action == Action.SEND_MESSAGE) {
                String user = input.split(" ")[0];
                input = input.substring(user.length());
                args = new String[]{input};
            } else {
                args = input.split(" ");
            }
        }

        Command command = new Command(action, args);

        if(isCommandValid(command)) {
            return command;
        }
        else {
            System.out.println("The command args are invalid. Please type 'help' to see the proper syntax");
            return null;
        }
    }

    private boolean isCommandValid(Command command) {
        switch(command.getAction()) {
            //No args
            case CHANGE_PASSWORD:
            case INCOMING_FRIEND_REQUESTS:
            case OUTGOING_FRIEND_REQUESTS:
            case LIST_FRIENDS:
            case LIST_BLOCKS:
            case LIST_UNREAD:
            case MARK_ALL_READ:
            case INCOMING_GAME_REQUESTS:
            case OUTGOING_GAME_REQUESTS:
            case GET_RANDOM_GAME_REQUEST_SIZE:
            case DELETE_RANDOM_GAME_REQUEST:
            case GET_OPEN_GAMES:
            case GET_GAMES_MY_TURN:
            case FORGET_SERVER:
            case HELP:
            case LOGOUT:
            case EXIT:
                return command.getArgs().length == 0;
            //One arg
            case CHANGE_USERNAME:
            case LOGIN:
            case REQUEST_FRIEND:
            case CANCEL_FRIEND_REQUEST:
            case ACCEPT_FRIEND_REQUEST:
            case DENY_FRIEND_REQUEST:
            case UNFRIEND:
            case BLOCK:
            case UNBLOCK:
            case MESSAGES_FROM_USER:
            case ACCEPT_GAME_REQUEST:
            case DENY_GAME_REQUEST:
            case GET_GAME:
            case PLAY_TURN:
            case CONNECT_TO_SERVER:
                return command.getArgs().length == 1;
            //Send message
            case SEND_MESSAGE:
                return command.getArgs().length == 2;
            //Request game
            case REQUEST_GAME:
                if(command.getArgs().length == 4 && isValidBoardSize(command.getArg(1)) &&
                   isValidColor(command.getArg(2)) && isValidColor(command.getArg(3))) {
                    return true;
                }
                break;
            //Create random game
            case CREATE_RANDOM_GAME_REQUEST:
                if(command.getArgs().length == 1 && isValidBoardSize(command.getArg(0))) {
                    return true;
                }
                break;
            //Local game
            case LOCAL_GAME:
                if(command.getArgs().length == 5 && isValidBoardSize(command.getArg(0)) &&
                   isValidColor(command.getArg(1)) && isValidPlayerType(command.getArg(2)) &&
                   isValidPlayerType(command.getArg(3)) && isValidInt(command.getArg(4))) {
                    return true;
                }
                break;
            default:
                return false;

        }
        return false;
    }

    private boolean isValidInt(String str) {
        try {
            Integer.parseInt(str);
            return true;
        }
        catch(NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidBoardSize(String str) {
        if(!isValidInt(str)) {
            return false;
        }
        int size = Integer.parseInt(str);
        if(size == 3 || size == 4 || size == 5 || size == 6 || size == 8) {
            return true;
        }

        return false;
    }

    private boolean isValidColor(String str) {
        return str.equalsIgnoreCase("white") || str.equalsIgnoreCase("black");
    }

    private boolean isValidPlayerType(String str) {
        return str.equalsIgnoreCase("human") || str.equalsIgnoreCase("ai");
    }

    private Completer createCompleter(List<User> users) {
        StringsCompleter noArgs = new StringsCompleter(
                Action.CHANGE_PASSWORD.getShorthand(),
                Action.INCOMING_FRIEND_REQUESTS.getShorthand(),
                Action.OUTGOING_FRIEND_REQUESTS.getShorthand(),
                Action.LIST_FRIENDS.getShorthand(),
                Action.LIST_BLOCKS.getShorthand(),
                Action.LIST_UNREAD.getShorthand(),
                Action.MARK_ALL_READ.getShorthand(),
                Action.INCOMING_GAME_REQUESTS.getShorthand(),
                Action.OUTGOING_GAME_REQUESTS.getShorthand(),
                Action.GET_RANDOM_GAME_REQUEST_SIZE.getShorthand(),
                Action.DELETE_RANDOM_GAME_REQUEST.getShorthand(),
                Action.GET_OPEN_GAMES.getShorthand(),
                Action.GET_GAMES_MY_TURN.getShorthand(),
                Action.CONNECT_TO_SERVER.getShorthand(),
                Action.FORGET_SERVER.getShorthand(),
                Action.HELP.getShorthand(),
                Action.LOGOUT.getShorthand(),
                Action.EXIT.getShorthand());
        StringsCompleter oneArg = new StringsCompleter(
                Action.CHANGE_USERNAME.getShorthand(),
                Action.LOGIN.getShorthand());
        StringsCompleter oneNameArg = new StringsCompleter(
                Action.REQUEST_FRIEND.getShorthand(),
                Action.CANCEL_FRIEND_REQUEST.getShorthand(),
                Action.ACCEPT_FRIEND_REQUEST.getShorthand(),
                Action.DENY_FRIEND_REQUEST.getShorthand(),
                Action.UNFRIEND.getShorthand(),
                Action.BLOCK.getShorthand(),
                Action.UNBLOCK.getShorthand(),
                Action.MESSAGES_FROM_USER.getShorthand(),
                Action.CANCEL_GAME_REQUEST.getShorthand(),
                Action.ACCEPT_GAME_REQUEST.getShorthand(),
                Action.DENY_GAME_REQUEST.getShorthand(),
                Action.GET_GAME.getShorthand(),
                Action.PLAY_TURN.getShorthand());
        StringsCompleter sendMessage = new StringsCompleter(
                Action.SEND_MESSAGE.getShorthand());
        StringsCompleter gameRequest = new StringsCompleter(
                Action.REQUEST_GAME.getShorthand());
        StringsCompleter randomGameRequest = new StringsCompleter(
                Action.CREATE_RANDOM_GAME_REQUEST.getShorthand());
        StringsCompleter localPlay = new StringsCompleter(
                Action.LOCAL_GAME.getShorthand());
        StringsCompleter colors = new StringsCompleter("white", "black");
        StringsCompleter sizes = new StringsCompleter("3", "4", "5", "6", "8");
        StringsCompleter playerTypes = new StringsCompleter("human", "ai");

        Map<String, Completer> comp = new HashMap<>();
        comp.put("NoArg", noArgs);
        comp.put("OneArg", oneArg);
        comp.put("OneNameArg", oneNameArg);
        comp.put("SendMessage", sendMessage);
        comp.put("GameRequest", gameRequest);
        comp.put("RandomRequest", randomGameRequest);
        comp.put("Local", localPlay);
        comp.put("Name", new UsernameCompleter(users));
        comp.put("Color", colors);
        comp.put("Size", sizes);
        comp.put("PlayerType", playerTypes);
        comp.put("Anything", new NullCompleter());

        String syntax = "NoArg | OneArg Anything | OneNameArg Name | SendMessage Name Anything | " +
                        "GameRequest Name Size Color Color | RandomRequest Size | " +
                        "Local Size Color PlayerType PlayerType Anything";
        return new RegexCompleter(syntax, comp::get);
    }
}
