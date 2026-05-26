package com.example.nextoffer.tracker;

public class ApplicationTrackerContext {

    private ApplicationState state = new NewApplicationState();

    public ApplicationStatus getStatus() {
        return state.getStatus();
    }

    public void setState(ApplicationState state) {
        this.state = state;
    }

    public void view() {
        state.view(this);
    }

    public void apply() {
        state.apply(this);
    }

    public void reject() {
        state.reject(this);
    }
}
