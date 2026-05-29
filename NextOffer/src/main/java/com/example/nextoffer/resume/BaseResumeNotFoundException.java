package com.example.nextoffer.resume;

public class BaseResumeNotFoundException extends RuntimeException {

    public BaseResumeNotFoundException() {
        super("Upload a base resume before generating tailored versions");
    }
}
