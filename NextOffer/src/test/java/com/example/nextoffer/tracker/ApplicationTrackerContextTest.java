package com.example.nextoffer.tracker;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApplicationTrackerContextTest {

    @Test
    void transitionsFromNewToViewedUsingStatePattern() {
        ApplicationTrackerContext context = ApplicationTrackerContext.fromStatus(ApplicationStatus.NEW);
        context.transitionTo(ApplicationStatus.VIEWED);
        assertEquals(ApplicationStatus.VIEWED, context.getStatus());
    }

    @Test
    void transitionsFromNewToAppliedInOneStep() {
        ApplicationTrackerContext context = ApplicationTrackerContext.fromStatus(ApplicationStatus.NEW);
        context.transitionTo(ApplicationStatus.APPLIED);
        assertEquals(ApplicationStatus.APPLIED, context.getStatus());
    }

    @Test
    void allowsCorrectionFromAppliedToViewed() {
        ApplicationTrackerContext context = ApplicationTrackerContext.fromStatus(ApplicationStatus.APPLIED);
        context.transitionTo(ApplicationStatus.VIEWED);
        assertEquals(ApplicationStatus.VIEWED, context.getStatus());
    }

    @Test
    void transitionsToRejectedFromApplied() {
        ApplicationTrackerContext context = ApplicationTrackerContext.fromStatus(ApplicationStatus.APPLIED);
        context.transitionTo(ApplicationStatus.REJECTED);
        assertEquals(ApplicationStatus.REJECTED, context.getStatus());
    }
}
