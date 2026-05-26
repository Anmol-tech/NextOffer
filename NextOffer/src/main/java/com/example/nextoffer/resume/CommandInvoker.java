package com.example.nextoffer.resume;

import java.util.ArrayDeque;
import java.util.Deque;

public class CommandInvoker {

    private final Deque<ResumeCommand> history = new ArrayDeque<>();

    public void run(ResumeCommand command) {
        command.execute();
        history.push(command);
    }

    public void undoLast() {
        if (!history.isEmpty()) {
            history.pop().undo();
        }
    }
}
