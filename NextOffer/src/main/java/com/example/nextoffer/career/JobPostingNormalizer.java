package com.example.nextoffer.career;

import com.example.nextoffer.job.JobPostingDto;

public interface JobPostingNormalizer {

    JobPostingDto normalize(JobPostingDto raw);
}
