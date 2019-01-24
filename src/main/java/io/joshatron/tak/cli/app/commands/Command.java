package io.joshatron.tak.cli.app.commands;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Command {
    private Action action;
    private String[] arguments;
}
