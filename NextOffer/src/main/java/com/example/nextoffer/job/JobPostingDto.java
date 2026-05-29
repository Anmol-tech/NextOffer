package com.example.nextoffer.job;

import java.time.Instant;

/**
 * Normalized job posting used across adapters, decorators, and the dashboard.
 */
public record JobPostingDto(
        String externalId,
        String companyName,
        String title,
        String location,
        String applyUrl,
        String description,
        Instant firstSeenAt,
        String department
) {
    public JobPostingDto(
            String externalId,
            String companyName,
            String title,
            String location,
            String applyUrl,
            String description,
            Instant firstSeenAt) {
        this(externalId, companyName, title, location, applyUrl, description, firstSeenAt, null);
    }
}
