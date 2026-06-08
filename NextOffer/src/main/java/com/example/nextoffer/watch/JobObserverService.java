package com.example.nextoffer.watch;

import com.example.nextoffer.career.AtsType;
import com.example.nextoffer.job.JobPosting;
import com.example.nextoffer.job.JobPostingDto;
import com.example.nextoffer.job.JobPostingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class JobObserverService {

    private final CompanyWatchRepository companyWatchRepository;
    private final JobPostingRepository jobPostingRepository;
    private final JobIngestionMediator jobIngestionMediator;

    public JobObserverService(
            CompanyWatchRepository companyWatchRepository,
            JobPostingRepository jobPostingRepository,
            JobIngestionMediator jobIngestionMediator) {
        this.companyWatchRepository = companyWatchRepository;
        this.jobPostingRepository = jobPostingRepository;
        this.jobIngestionMediator = jobIngestionMediator;
    }

    @Transactional
    public PollResult pollWatch(Long watchId, Long userId) {
        CompanyWatch watch = companyWatchRepository.findByIdAndUserId(watchId, userId)
                .orElseThrow(() -> new WatchNotFoundException(watchId));
        return pollWatchInternal(watch);
    }

    @Transactional
    public void pollAllEnabledWatches() {
        for (CompanyWatch watch : companyWatchRepository.findByEnabledTrue()) {
            pollWatchInternal(watch);
        }
    }

    private PollResult pollWatchInternal(CompanyWatch watch) {
        if (!watch.isEnabled()) {
            return new PollResult(watch.getId(), 0, List.of());
        }
        try {
            Set<String> knownIds = new HashSet<>(jobPostingRepository.findExternalIdsByCompanyWatchId(watch.getId()));
            JobIngestionMediator.PollOutcome outcome = jobIngestionMediator.pollWatch(watch, knownIds, true);
            refreshWorkdayApplyUrls(watch, outcome.allFetched());
            watch.setLastCheckedAt(Instant.now());
            watch.setLastScanStatus(ScanStatus.SUCCESS);
            watch.setLastErrorMessage(null);
            companyWatchRepository.save(watch);
            return new PollResult(watch.getId(), outcome.newJobs().size(), outcome.newJobs());
        } catch (Exception ex) {
            watch.setLastCheckedAt(Instant.now());
            watch.setLastScanStatus(ScanStatus.FAILED);
            watch.setLastErrorMessage(truncate(friendlyMessage(ex), 1000));
            companyWatchRepository.save(watch);
            throw new WatchPollException(watch.getId(), ex);
        }
    }

    private void refreshWorkdayApplyUrls(CompanyWatch watch, List<JobPostingDto> fetched) {
        if (watch.getAtsType() != AtsType.WORKDAY || fetched.isEmpty()) {
            return;
        }
        Map<String, JobPostingDto> byExternalId = new HashMap<>();
        for (JobPostingDto dto : fetched) {
            byExternalId.put(dto.externalId(), dto);
        }
        for (JobPosting posting : jobPostingRepository.findByCompanyWatchId(watch.getId())) {
            JobPostingDto latest = byExternalId.get(posting.getExternalId());
            if (latest != null && !latest.applyUrl().equals(posting.getApplyUrl())) {
                posting.setApplyUrl(latest.applyUrl());
                jobPostingRepository.save(posting);
            }
        }
    }

    private static String friendlyMessage(Throwable ex) {
        Throwable current = ex;
        while (current != null) {
            if (current.getMessage() != null && !current.getMessage().isBlank()) {
                return current.getMessage();
            }
            current = current.getCause();
        }
        return "Unknown error";
    }

    private static String truncate(String message, int max) {
        if (message == null) {
            return "Unknown error";
        }
        return message.length() <= max ? message : message.substring(0, max);
    }

    public record PollResult(Long watchId, int newJobsCount, List<JobPostingDto> newJobs) {
    }
}
