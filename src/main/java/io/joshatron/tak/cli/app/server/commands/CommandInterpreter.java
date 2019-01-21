package io.joshatron.tak.cli.app.server.commands;

import io.joshatron.tak.cli.app.server.response.User;
import org.jline.reader.Completer;
import org.jline.builtins.Completers.RegexCompleter;
import org.jline.reader.impl.completer.NullCompleter;
import org.jline.reader.impl.completer.StringsCompleter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CommandInterpreter {

    public static Completer getCommandCompleter(ArrayList<User> users) {
        StringsCompleter noArgs = new StringsCompleter(
                ServerActions.INCOMING_FRIEND_REQUESTS.getShorthand(),
                ServerActions.OUTGOING_FRIEND_REQUESTS.getShorthand(),
                ServerActions.LIST_FRIENDS.getShorthand(),
                ServerActions.LIST_BLOCKS.getShorthand(),
                ServerActions.LIST_UNREAD.getShorthand(),
                ServerActions.MARK_ALL_READ.getShorthand(),
                ServerActions.INCOMING_GAME_REQUESTS.getShorthand(),
                ServerActions.OUTGOING_GAME_REQUESTS.getShorthand(),
                ServerActions.GET_RANDOM_GAME_REQUEST_SIZE.getShorthand(),
                ServerActions.DELETE_RANDOM_GAME_REQUEST.getShorthand(),
                ServerActions.GET_OPEN_GAMES.getShorthand(),
                ServerActions.GET_GAMES_MY_TURN.getShorthand(),
                ServerActions.HELP.getShorthand(),
                ServerActions.LOGOUT.getShorthand(),
                ServerActions.EXIT.getShorthand());
        StringsCompleter oneArg = new StringsCompleter(
                ServerActions.CHANGE_PASSWORD.getShorthand(),
                ServerActions.CHANGE_USERNAME.getShorthand());
        StringsCompleter oneNameArg = new StringsCompleter(
                ServerActions.REQUEST_FRIEND.getShorthand(),
                ServerActions.CANCEL_FRIEND_REQUEST.getShorthand(),
                ServerActions.ACCEPT_FRIEND_REQUEST.getShorthand(),
                ServerActions.DENY_FRIEND_REQUEST.getShorthand(),
                ServerActions.UNFRIEND.getShorthand(),
                ServerActions.BLOCK.getShorthand(),
                ServerActions.UNBLOCK.getShorthand(),
                ServerActions.MESSAGES_FROM_USER.getShorthand(),
                ServerActions.CANCEL_GAME_REQUEST.getShorthand(),
                ServerActions.ACCEPT_GAME_REQUEST.getShorthand(),
                ServerActions.DENY_GAME_REQUEST.getShorthand(),
                ServerActions.GET_GAME.getShorthand(),
                ServerActions.PLAY_TURN.getShorthand());
        StringsCompleter sendMessage = new StringsCompleter(
                ServerActions.SEND_MESSAGE.getShorthand());
        StringsCompleter gameRequest = new StringsCompleter(
                ServerActions.REQUEST_GAME.getShorthand());
        StringsCompleter randomGameRequest = new StringsCompleter(
                ServerActions.CREATE_RANDOM_GAME_REQUEST.getShorthand());
        StringsCompleter colors = new StringsCompleter("white", "black");
        StringsCompleter sizes = new StringsCompleter("3", "4", "5", "6", "8");

        Map<String, Completer> comp = new HashMap<>();
        comp.put("NoArg", noArgs);
        comp.put("OneArg", oneArg);
        comp.put("OneNameArg", oneNameArg);
        comp.put("SendMessage", sendMessage);
        comp.put("GameRequest", gameRequest);
        comp.put("RandomRequest", randomGameRequest);
        comp.put("Name", new UsernameCompleter(users));
        comp.put("Color", colors);
        comp.put("Size", sizes);
        comp.put("Anything", new NullCompleter());

        String syntax = "NoArg | OneArg Anything | OneNameArg Name | SendMessage Name Anything | " +
                        "GameRequest Name Size Color Color | RandomRequest Size";
        return new RegexCompleter(syntax, comp::get);
    }
}
