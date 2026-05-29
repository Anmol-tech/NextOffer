package com.example.nextoffer.resume;

public class ResumeGenerationException extends RuntimeException {

    public ResumeGenerationException(String message) {
        super(message);
    }

    public ResumeGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
