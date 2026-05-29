package com.example.nextoffer.resume;

import com.example.nextoffer.job.JobPostingDto;

/**
 * Template Method — defines tailoring steps; subclasses plug in AI specifics.
 */
public abstract class AbstractTailoringPipeline {

    public final TailoredResumeContent execute(JobPostingDto job, TailoredResumeContent base) {
        TailoredResumeContent extracted = extractKeywords(job, base);
        TailoredResumeContent reordered = reorderSections(job, extracted);
        return polishLanguage(reordered);
    }

    protected TailoredResumeContent extractKeywords(JobPostingDto job, TailoredResumeContent base) {
        return base;
    }

    protected abstract TailoredResumeContent reorderSections(JobPostingDto job, TailoredResumeContent content);

    protected TailoredResumeContent polishLanguage(TailoredResumeContent content) {
        return content;
    }
}
