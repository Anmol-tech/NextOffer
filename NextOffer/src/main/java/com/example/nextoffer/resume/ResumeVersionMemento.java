package com.example.nextoffer.resume;


/**
 * Memento — snapshot of tailored resume state.
 */
public record ResumeVersionMemento(TailoredResumeContent content, long savedAtEpochMs) {
}
