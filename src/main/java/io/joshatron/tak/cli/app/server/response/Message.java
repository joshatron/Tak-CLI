package io.joshatron.tak.cli.app.server.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class Message {
    private String sender;
    private String recipient;
    private Date timestamp;
    private String message;
    private boolean opened;
}
