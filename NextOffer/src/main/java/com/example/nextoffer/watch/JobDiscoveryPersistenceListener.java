package com.example.nextoffer.watch;

import com.example.nextoffer.job.JobPosting;
import com.example.nextoffer.job.JobPostingDto;
import com.example.nextoffer.job.JobPostingRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class JobDiscoveryPersistenceListener implements JobDiscoveryListener {

    private final CompanyWatchSubject watchSubject;
    private final CompanyWatchRepository companyWatchRepository;
    private final JobPostingRepository jobPostingRepository;

    public JobDiscoveryPersistenceListener(
            CompanyWatchSubject watchSubject,
            CompanyWatchRepository companyWatchRepository,
            JobPostingRepository jobPostingRepository) {
        this.watchSubject = watchSubject;
        this.companyWatchRepository = companyWatchRepository;
        this.jobPostingRepository = jobPostingRepository;
    }

    @PostConstruct
    void register() {
        watchSubject.subscribe(this);
    }

    @Override
    @Transactional
    public void onNewJobDiscovered(JobPostingDto job, Long companyWatchId) {
        CompanyWatch watch = companyWatchRepository.findById(companyWatchId)
                .orElseThrow(() -> new IllegalStateException("Company watch not found: " + companyWatchId));
        if (jobPostingRepository.existsByCompanyWatchIdAndExternalId(companyWatchId, job.externalId())) {
            return;
        }
        jobPostingRepository.save(new JobPosting(watch, job));
    }
}
