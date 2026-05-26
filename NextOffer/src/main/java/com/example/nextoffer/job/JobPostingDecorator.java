package com.example.nextoffer.job;

import com.example.nextoffer.job.JobPostingDto;

/**
 * Decorator — wraps a job posting with optional presentation metadata.
 */
public abstract class JobPostingDecorator {

    protected final JobPostingDto delegate;

    protected JobPostingDecorator(JobPostingDto delegate) {
        this.delegate = delegate;
    }

    public JobPostingDto getDelegate() {
        return delegate;
    }

    public abstract String displayTitle();
}
