package com.example.nextoffer.resume;

/**
 * Command — encapsulates a resume-related action.
 */
public interface ResumeCommand {

    void execute();

    void undo();
}
