package io.joshatron.tak.cli.app.server.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SocialNotifications {
    private int friendRequests;
    private int unreadMessages;
}
