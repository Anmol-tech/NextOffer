package com.example.nextoffer.tracker;

public class AppliedApplicationState implements ApplicationState {

    @Override
    public ApplicationStatus getStatus() {
        return ApplicationStatus.APPLIED;
    }

    @Override
    public void view(ApplicationTrackerContext context) {
    }

    @Override
    public void apply(ApplicationTrackerContext context) {
    }

    @Override
    public void reject(ApplicationTrackerContext context) {
        context.setState(new RejectedApplicationState());
    }
}
