package com.example.nextoffer.watch;

import com.example.nextoffer.job.JobPostingDto;

/**
 * Observer — reacts when the watch list detects a new job.
 */
public interface JobDiscoveryListener {

    void onNewJobDiscovered(JobPostingDto job);
}
