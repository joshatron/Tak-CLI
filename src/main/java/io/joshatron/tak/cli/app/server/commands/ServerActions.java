package io.joshatron.tak.cli.app.server.commands;

public enum ServerActions {
    CHANGE_PASSWORD("cpass"),
    CHANGE_USERNAME("cname"),
    INCOMING_FRIEND_REQUESTS("ifreq"),
    OUTGOING_FRIEND_REQUESTS("ofreq"),
    LIST_FRIENDS("friends"),
    LIST_BLOCKS("blocks"),
    REQUEST_FRIEND("freq"),
    CANCEL_FRIEND_REQUEST("fcancel"),
    ACCEPT_FRIEND_REQUEST("faccept"),
    DENY_FRIEND_REQUEST("fdeny"),
    UNFRIEND("unfriend"),
    BLOCK("block"),
    UNBLOCK("unblock"),
    SEND_MESSAGE("msend"),
    LIST_UNREAD("munread"),
    MARK_ALL_READ("mread"),
    MESSAGES_FROM_USER("muser"),
    INCOMING_GAME_REQUESTS("igreq"),
    OUTGOING_GAME_REQUESTS("ogreq"),
    REQUEST_GAME("greq"),
    CANCEL_GAME_REQUEST("gcancel"),
    ACCEPT_GAME_REQUEST("gaccept"),
    DENY_GAME_REQUEST("gdeny"),
    GET_RANDOM_GAME_REQUEST_SIZE("grand"),
    CREATE_RANDOM_GAME_REQUEST("grandmake"),
    DELETE_RANDOM_GAME_REQUEST("granddel"),
    GET_OPEN_GAMES("games"),
    GET_GAMES_MY_TURN("myturn"),
    GET_GAME("game"),
    PLAY_TURN("play"),
    HELP("help"),
    LOGOUT("logout"),
    EXIT("exit");

    private String shorthand;

    ServerActions(String shorthand) {
        this.shorthand = shorthand;
    }

    public String getShorthand() {
        return shorthand;
    }
}
