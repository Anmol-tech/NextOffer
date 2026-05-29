package com.example.nextoffer.resume;

import com.example.nextoffer.job.JobPostingDto;
import org.springframework.stereotype.Component;

@Component
public class OpenAiTailoringPipeline extends AbstractTailoringPipeline {

    private final OpenAiClient openAiClient;
    private final RuleBasedTailoringPipeline fallbackPipeline;

    public OpenAiTailoringPipeline(OpenAiClient openAiClient, RuleBasedTailoringPipeline fallbackPipeline) {
        this.openAiClient = openAiClient;
        this.fallbackPipeline = fallbackPipeline;
    }

    @Override
    protected TailoredResumeContent reorderSections(JobPostingDto job, TailoredResumeContent content) {
        if (openAiClient.isEnabled()) {
            return openAiClient.tailor(job.title(), job.companyName(), job.description(), content);
        }
        return fallbackPipeline.reorderSections(job, content);
    }
}
