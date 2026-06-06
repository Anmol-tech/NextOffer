package com.example.nextoffer.tracker;

public class ApplicationTrackerContext {

    private ApplicationState state = new NewApplicationState();

    public static ApplicationTrackerContext fromStatus(ApplicationStatus status) {
        ApplicationTrackerContext context = new ApplicationTrackerContext();
        context.setState(ApplicationStates.from(status));
        return context;
    }

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

    /**
     * Moves toward the requested tracker column, using state transitions when possible
     * and falling back to a direct assignment for corrections (e.g. Applied → Viewed).
     */
    public void transitionTo(ApplicationStatus target) {
        if (getStatus() == target) {
            return;
        }
        for (int step = 0; step < 4 && getStatus() != target; step++) {
            if (!advanceOnceToward(target)) {
                break;
            }
        }
        if (getStatus() != target) {
            setState(ApplicationStates.from(target));
        }
    }

    private boolean advanceOnceToward(ApplicationStatus target) {
        ApplicationStatus current = getStatus();
        if (current == target) {
            return false;
        }
        return switch (target) {
            case NEW -> {
                setState(new NewApplicationState());
                yield false;
            }
            case VIEWED -> {
                if (current == ApplicationStatus.NEW) {
                    view();
                    yield true;
                }
                setState(new ViewedApplicationState());
                yield false;
            }
            case APPLIED -> {
                if (current == ApplicationStatus.NEW || current == ApplicationStatus.VIEWED) {
                    apply();
                    yield true;
                }
                setState(new AppliedApplicationState());
                yield false;
            }
            case REJECTED -> {
                reject();
                yield getStatus() == ApplicationStatus.REJECTED;
            }
        };
    }
}
