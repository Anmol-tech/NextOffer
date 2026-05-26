package com.example.nextoffer.resume;


public class OpenAiTailoringPipeline extends AbstractTailoringPipeline {

    @Override
    protected TailoredResumeContent reorderSections(TailoredResumeContent content) {
        // TODO: call OpenAI with structured prompts
        return new TailoredResumeBuilder()
                .summary(content.summary())
                .build();
    }
}
