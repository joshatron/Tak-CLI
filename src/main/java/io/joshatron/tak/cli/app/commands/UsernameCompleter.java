package io.joshatron.tak.cli.app.commands;

import io.joshatron.tak.cli.app.server.response.User;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.utils.AttributedString;

import java.util.ArrayList;
import java.util.List;

public class UsernameCompleter implements Completer {

    private ArrayList<User> users;

    public UsernameCompleter(ArrayList<User> users) {
        this.users = users;
    }

    @Override
    public void complete(LineReader lineReader, ParsedLine parsedLine, List<Candidate> list) {
        if(parsedLine != null && list != null) {
            ArrayList<Candidate> candidates = new ArrayList<>();

            for(User user : users) {
                candidates.add(new Candidate(AttributedString.stripAnsi(user.getUsername()), user.getUsername(), null, null, null, null, true));
            }

            list.addAll(candidates);
        }
    }
}
