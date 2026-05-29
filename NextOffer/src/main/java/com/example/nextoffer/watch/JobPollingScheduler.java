package com.example.nextoffer.watch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class JobPollingScheduler {

    private static final Logger log = LoggerFactory.getLogger(JobPollingScheduler.class);

    private final JobObserverService jobObserverService;

    public JobPollingScheduler(JobObserverService jobObserverService) {
        this.jobObserverService = jobObserverService;
    }

    @Scheduled(fixedDelayString = "${app.watch.poll-interval-ms:900000}")
    public void pollEnabledWatches() {
        log.debug("Starting scheduled poll of enabled company watches");
        try {
            jobObserverService.pollAllEnabledWatches();
        } catch (Exception ex) {
            log.warn("Scheduled watch poll encountered errors: {}", ex.getMessage());
        }
    }
}
