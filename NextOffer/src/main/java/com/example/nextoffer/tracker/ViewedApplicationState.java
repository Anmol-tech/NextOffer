package com.example.nextoffer.tracker;

public class ViewedApplicationState implements ApplicationState {

    @Override
    public ApplicationStatus getStatus() {
        return ApplicationStatus.VIEWED;
    }

    @Override
    public void view(ApplicationTrackerContext context) {
        // already viewed
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
