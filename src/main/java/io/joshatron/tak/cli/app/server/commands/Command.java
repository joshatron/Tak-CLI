package io.joshatron.tak.cli.app.server.commands;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Command {
    private Actions action;
    private String[] arguments;
}
