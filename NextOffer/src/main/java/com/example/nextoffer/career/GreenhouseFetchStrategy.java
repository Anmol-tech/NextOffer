package com.example.nextoffer.career;

import com.example.nextoffer.job.JobPostingDto;

import java.time.Instant;
import java.util.List;

public class GreenhouseFetchStrategy implements CareerPageFetchStrategy {

    @Override
    public List<JobPostingDto> fetch(String careerPageUrl) {
        // TODO: HTTP call to Greenhouse boards API
        return List.of(new JobPostingDto(
                "gh-stub-1",
                "Watched Company",
                "Software Engineer Intern",
                "Remote",
                careerPageUrl,
                "Stub job description",
                Instant.now()
        ));
    }
}
