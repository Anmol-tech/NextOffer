package com.example.nextoffer.resume;

public class TailoredResumeNotFoundException extends RuntimeException {

    public TailoredResumeNotFoundException(Long id) {
        super("Tailored resume not found: " + id);
    }
}
