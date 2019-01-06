package io.joshatron.tak.cli.app.server.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GameNotifications {
    private int gameRequests;
    private int yourTurn;
}
