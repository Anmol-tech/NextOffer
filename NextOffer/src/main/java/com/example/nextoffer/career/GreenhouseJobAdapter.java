package com.example.nextoffer.career;

import com.example.nextoffer.job.JobPostingDto;

import com.example.nextoffer.job.JobPostingDto;

import java.time.Instant;

/**
 * Adapter — maps Greenhouse (or similar) JSON to {@link JobPostingDto}.
 */
public final class GreenhouseJobAdapter implements JobDescriptionParser {

    public static JobPostingDto fromJson(String rawPayload) {
        // Placeholder until HTTP client + JSON parser are wired.
        return new JobPostingDto(
                "gh-placeholder",
                "Target Company",
                "Software Engineer",
                "Remote",
                "https://example.com/apply",
                rawPayload == null ? "" : rawPayload,
                Instant.now()
        );
    }

    @Override
    public JobPostingDto parse(String rawPayload) {
        return fromJson(rawPayload);
    }
}
