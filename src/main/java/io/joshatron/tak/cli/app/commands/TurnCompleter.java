package io.joshatron.tak.cli.app.commands;

import io.joshatron.tak.engine.turn.Turn;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TurnCompleter implements Completer {

    private List<String> turns;

    public TurnCompleter(List<Turn> turns) {
        this.turns = turns.stream().map(Turn::toString).collect(Collectors.toList());
    }

    @Override
    public void complete(LineReader lineReader, ParsedLine parsedLine, List<Candidate> list) {
        if(parsedLine != null && list != null) {
            ArrayList<Candidate> candidates = new ArrayList<>();

            List<String> possible = turns.stream().filter(element -> element.startsWith(parsedLine.line())).collect(Collectors.toList());
            for(String turn : possible) {
                String option = turn.split(" ")[parsedLine.words().size() - 1];
                candidates.add(new Candidate(option, option, null, null, null, null, true));
            }

            list.addAll(candidates);
        }
    }
}
