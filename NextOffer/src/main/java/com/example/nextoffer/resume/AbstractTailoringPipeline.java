package com.example.nextoffer.resume;

import com.example.nextoffer.job.JobPostingDto;

import com.example.nextoffer.job.JobPostingDto;

/**
 * Template Method — defines tailoring steps; subclasses implement AI specifics.
 */
public abstract class AbstractTailoringPipeline {

    public final TailoredResumeContent execute(JobPostingDto job, TailoredResumeContent base) {
        TailoredResumeContent extracted = extractKeywords(job, base);
        TailoredResumeContent reordered = reorderSections(extracted);
        return polishLanguage(reordered);
    }

    protected TailoredResumeContent extractKeywords(JobPostingDto job, TailoredResumeContent base) {
        return base;
    }

    protected abstract TailoredResumeContent reorderSections(TailoredResumeContent content);

    protected TailoredResumeContent polishLanguage(TailoredResumeContent content) {
        return content;
    }
}
