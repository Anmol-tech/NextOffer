package com.example.nextoffer.watch.dto;

import com.example.nextoffer.job.JobPostingResponse;
import com.example.nextoffer.watch.JobObserverService;

import java.util.List;

public record PollWatchResponse(Long watchId, int newJobsCount, List<JobPostingResponse> newJobs) {

    public static PollWatchResponse from(JobObserverService.PollResult result) {
        List<JobPostingResponse> jobs = result.newJobs().stream()
                .map(dto -> new JobPostingResponse(
                        null,
                        result.watchId(),
                        dto.externalId(),
                        dto.companyName(),
                        dto.title(),
                        dto.location(),
                        dto.applyUrl(),
                        dto.description(),
                        dto.firstSeenAt()
                ))
                .toList();
        return new PollWatchResponse(result.watchId(), result.newJobsCount(), jobs);
    }
}
