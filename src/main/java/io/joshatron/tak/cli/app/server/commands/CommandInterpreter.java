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
                Actions.INCOMING_FRIEND_REQUESTS.getShorthand(),
                Actions.OUTGOING_FRIEND_REQUESTS.getShorthand(),
                Actions.LIST_FRIENDS.getShorthand(),
                Actions.LIST_BLOCKS.getShorthand(),
                Actions.LIST_UNREAD.getShorthand(),
                Actions.MARK_ALL_READ.getShorthand(),
                Actions.INCOMING_GAME_REQUESTS.getShorthand(),
                Actions.OUTGOING_GAME_REQUESTS.getShorthand(),
                Actions.GET_RANDOM_GAME_REQUEST_SIZE.getShorthand(),
                Actions.DELETE_RANDOM_GAME_REQUEST.getShorthand(),
                Actions.GET_OPEN_GAMES.getShorthand(),
                Actions.GET_GAMES_MY_TURN.getShorthand(),
                Actions.FORGET_SERVER.getShorthand(),
                Actions.HELP.getShorthand(),
                Actions.LOGOUT.getShorthand(),
                Actions.EXIT.getShorthand());
        StringsCompleter oneArg = new StringsCompleter(
                Actions.CHANGE_PASSWORD.getShorthand(),
                Actions.CHANGE_USERNAME.getShorthand());
        StringsCompleter oneNameArg = new StringsCompleter(
                Actions.REQUEST_FRIEND.getShorthand(),
                Actions.CANCEL_FRIEND_REQUEST.getShorthand(),
                Actions.ACCEPT_FRIEND_REQUEST.getShorthand(),
                Actions.DENY_FRIEND_REQUEST.getShorthand(),
                Actions.UNFRIEND.getShorthand(),
                Actions.BLOCK.getShorthand(),
                Actions.UNBLOCK.getShorthand(),
                Actions.MESSAGES_FROM_USER.getShorthand(),
                Actions.CANCEL_GAME_REQUEST.getShorthand(),
                Actions.ACCEPT_GAME_REQUEST.getShorthand(),
                Actions.DENY_GAME_REQUEST.getShorthand(),
                Actions.GET_GAME.getShorthand(),
                Actions.PLAY_TURN.getShorthand());
        StringsCompleter sendMessage = new StringsCompleter(
                Actions.SEND_MESSAGE.getShorthand());
        StringsCompleter gameRequest = new StringsCompleter(
                Actions.REQUEST_GAME.getShorthand());
        StringsCompleter randomGameRequest = new StringsCompleter(
                Actions.CREATE_RANDOM_GAME_REQUEST.getShorthand());
        StringsCompleter connectToServer = new StringsCompleter(
                Actions.CONNECT_TO_SERVER.getShorthand());
        StringsCompleter localPlay = new StringsCompleter(
                Actions.LOCAL_GAME.getShorthand());
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
