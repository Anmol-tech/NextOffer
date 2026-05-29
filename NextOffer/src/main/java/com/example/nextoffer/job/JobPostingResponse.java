package com.example.nextoffer.job;

import java.time.Instant;

public record JobPostingResponse(
        Long id,
        Long companyWatchId,
        String externalId,
        String companyName,
        String title,
        String location,
        String applyUrl,
        String description,
        Instant firstSeenAt
) {
    public static JobPostingResponse from(JobPosting job) {
        return new JobPostingResponse(
                job.getId(),
                job.getCompanyWatch().getId(),
                job.getExternalId(),
                job.getCompanyName(),
                job.getTitle(),
                job.getLocation(),
                job.getApplyUrl(),
                job.getDescription(),
                job.getFirstSeenAt()
        );
    }
}
