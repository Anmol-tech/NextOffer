package com.example.nextoffer.resume;


/**
 * Chain of Responsibility — ordered resume validation handlers.
 */
public abstract class ResumeValidationHandler {

    private ResumeValidationHandler next;

    public ResumeValidationHandler linkWith(ResumeValidationHandler next) {
        this.next = next;
        return next;
    }

    public void validate(TailoredResumeContent content) {
        doValidate(content);
        if (next != null) {
            next.validate(content);
        }
    }

    protected abstract void doValidate(TailoredResumeContent content);
}
