package com.example.nextoffer.watch;

import com.example.nextoffer.job.JobPostingDto;

import java.util.ArrayList;
import java.util.List;

/**
 * Observer — subject that notifies listeners of newly discovered jobs.
 */
public class CompanyWatchSubject {

    private final List<JobDiscoveryListener> listeners = new ArrayList<>();

    public void subscribe(JobDiscoveryListener listener) {
        listeners.add(listener);
    }

    public void notifyNewJob(JobPostingDto job) {
        listeners.forEach(listener -> listener.onNewJobDiscovered(job));
    }
}
