package com.example.nextoffer.resume;

import java.util.ArrayDeque;
import java.util.Deque;

public class ResumeHistoryCaretaker {

    private final Deque<ResumeVersionMemento> history = new ArrayDeque<>();

    public void save(ResumeVersionMemento memento) {
        history.push(memento);
    }

    public ResumeVersionMemento restorePrevious() {
        return history.isEmpty() ? null : history.pop();
    }
}
