package com.example.nextoffer.watch;

import com.example.nextoffer.career.CareerPageFetchStrategy;
import com.example.nextoffer.career.GreenhouseJobAdapter;
import com.example.nextoffer.job.JobPostingDto;

import com.example.nextoffer.job.JobPostingDto;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Mediator — coordinates fetch, diff, notify without coupling components.
 */
public class JobIngestionMediator {

    private final CareerPageFetchStrategy fetchStrategy;
    private final CompanyWatchSubject watchSubject;
    private final Set<String> knownJobIds = new HashSet<>();
    private final GreenhouseJobAdapter adapter = new GreenhouseJobAdapter();

    public JobIngestionMediator(CareerPageFetchStrategy fetchStrategy, CompanyWatchSubject watchSubject) {
        this.fetchStrategy = fetchStrategy;
        this.watchSubject = watchSubject;
    }

    public void pollCareerPage(String careerPageUrl) {
        List<JobPostingDto> postings = fetchStrategy.fetch(careerPageUrl);
        for (JobPostingDto posting : postings) {
            if (knownJobIds.add(posting.externalId())) {
                JobPostingDto normalized = adapter.parse(posting.description());
                watchSubject.notifyNewJob(normalized);
            }
        }
    }
}
