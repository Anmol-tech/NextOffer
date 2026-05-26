package com.example.nextoffer.tracker;

/**
 * State — encapsulates behavior for each application tracker phase.
 */
public interface ApplicationState {

    ApplicationStatus getStatus();

    void view(ApplicationTrackerContext context);

    void apply(ApplicationTrackerContext context);

    void reject(ApplicationTrackerContext context);
}
