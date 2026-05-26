package com.example.nextoffer.tracker;

public class RejectedApplicationState implements ApplicationState {

    @Override
    public ApplicationStatus getStatus() {
        return ApplicationStatus.REJECTED;
    }

    @Override
    public void view(ApplicationTrackerContext context) {
    }

    @Override
    public void apply(ApplicationTrackerContext context) {
    }

    @Override
    public void reject(ApplicationTrackerContext context) {
    }
}
