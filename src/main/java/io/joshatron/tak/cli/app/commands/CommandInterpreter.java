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

    //TODO: implement
    private boolean isCommandValid(Command command) {
        return true;
    }

    private Completer createCompleter(List<User> users) {
        StringsCompleter noArgs = new StringsCompleter(
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
                Action.FORGET_SERVER.getShorthand(),
                Action.HELP.getShorthand(),
                Action.LOGOUT.getShorthand(),
                Action.EXIT.getShorthand());
        StringsCompleter oneArg = new StringsCompleter(
                Action.CHANGE_PASSWORD.getShorthand(),
                Action.CHANGE_USERNAME.getShorthand());
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
        StringsCompleter connectToServer = new StringsCompleter(
                Action.CONNECT_TO_SERVER.getShorthand());
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
        comp.put("Connect", connectToServer);
        comp.put("Local", localPlay);
        comp.put("Name", new UsernameCompleter(users));
        comp.put("Color", colors);
        comp.put("Size", sizes);
        comp.put("PlayerType", playerTypes);
        comp.put("Anything", new NullCompleter());

        String syntax = "NoArg | OneArg Anything | OneNameArg Name | SendMessage Name Anything | " +
                        "GameRequest Name Size Color Color | RandomRequest Size | Connect Anything |" +
                        "Local Size Color PlayerType PlayerType Anything";
        return new RegexCompleter(syntax, comp::get);
    }
}
