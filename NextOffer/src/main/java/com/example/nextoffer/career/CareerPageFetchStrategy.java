package com.example.nextoffer.career;

import com.example.nextoffer.job.JobPostingDto;
import com.example.nextoffer.watch.CompanyWatch;

import java.util.List;

/**
 * Strategy — interchangeable career-page fetch algorithms.
 */
public interface CareerPageFetchStrategy {

    List<JobPostingDto> fetch(String careerPageUrl);

    default List<JobPostingDto> fetchForWatch(CompanyWatch watch) {
        return fetch(watch.getCareerPageUrl());
    }
}
