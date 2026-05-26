package com.example.nextoffer.career;

import com.example.nextoffer.job.JobPostingDto;

public interface JobDescriptionParser {

    JobPostingDto parse(String rawPayload);
}
