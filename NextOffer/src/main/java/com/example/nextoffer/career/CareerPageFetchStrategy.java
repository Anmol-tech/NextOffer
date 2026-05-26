package com.example.nextoffer.career;

import com.example.nextoffer.job.JobPostingDto;

import java.util.List;

/**
 * Strategy — interchangeable career-page fetch algorithms.
 */
public interface CareerPageFetchStrategy {

    List<JobPostingDto> fetch(String careerPageUrl);
}
