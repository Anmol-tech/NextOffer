package com.example.nextoffer.career;

import com.example.nextoffer.job.JobPostingDto;

import java.util.List;

public interface CareerPageFetcher {

    List<JobPostingDto> fetchOpenings(String careerPageUrl);
}
