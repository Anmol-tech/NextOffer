package com.example.nextoffer.tracker;

public class NewApplicationState implements ApplicationState {

    @Override
    public ApplicationStatus getStatus() {
        return ApplicationStatus.NEW;
    }

    @Override
    public void view(ApplicationTrackerContext context) {
        context.setState(new ViewedApplicationState());
    }

    @Override
    public void apply(ApplicationTrackerContext context) {
        context.setState(new AppliedApplicationState());
    }

    @Override
    public void reject(ApplicationTrackerContext context) {
        context.setState(new RejectedApplicationState());
    }
}
