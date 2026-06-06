package com.example.nextoffer.tracker;

public final class ApplicationStates {

    private ApplicationStates() {
    }

    public static ApplicationState from(ApplicationStatus status) {
        return switch (status) {
            case NEW -> new NewApplicationState();
            case VIEWED -> new ViewedApplicationState();
            case APPLIED -> new AppliedApplicationState();
            case REJECTED -> new RejectedApplicationState();
        };
    }
}
