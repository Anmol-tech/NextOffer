package com.example.nextoffer.watch;

import com.example.nextoffer.career.CareerPageFetchStrategy;
import com.example.nextoffer.career.CareerPageFetchStrategyFactory;
import com.example.nextoffer.job.JobPostingDto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Mediator — coordinates fetch, diff, and new-job detection for one company watch.
 */
public class JobIngestionMediator {

    private final CareerPageFetchStrategyFactory strategyFactory;
    private final CompanyWatchSubject watchSubject;

    public JobIngestionMediator(
            CareerPageFetchStrategyFactory strategyFactory,
            CompanyWatchSubject watchSubject) {
        this.strategyFactory = strategyFactory;
        this.watchSubject = watchSubject;
    }

    /**
     * @param knownExternalIds mutable set of IDs already stored; new IDs are added when detected
     */
    public List<JobPostingDto> pollWatch(
            CompanyWatch watch,
            Set<String> knownExternalIds,
            boolean notifyListeners) {
        CareerPageFetchStrategy strategy = strategyFactory.forAtsType(watch.getAtsType());
        List<JobPostingDto> fetched = strategy.fetchForWatch(watch);
        List<JobPostingDto> newJobs = new ArrayList<>();

        for (JobPostingDto posting : fetched) {
            if (knownExternalIds.add(posting.externalId())) {
                newJobs.add(posting);
                if (notifyListeners) {
                    watchSubject.notifyNewJob(posting, watch.getId());
                }
            }
        }
        return newJobs;
    }
}
