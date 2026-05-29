package com.example.nextoffer.resume;

import com.example.nextoffer.job.JobPostingDto;
import org.springframework.stereotype.Service;

/**
 * Facade — one entry for tailor + validate.
 */
@Service
public class ResumeTailoringFacade {

    private final OpenAiTailoringPipeline tailoringPipeline;
    private final ResumeValidationHandler validationChain;

    public ResumeTailoringFacade(OpenAiTailoringPipeline tailoringPipeline) {
        this.tailoringPipeline = tailoringPipeline;
        ResumeValidationHandler fabrication = new NoFabricationValidationHandler();
        fabrication.linkWith(new AtsLengthValidationHandler());
        this.validationChain = fabrication;
    }

    public TailoredResumeContent tailor(JobPostingDto job, TailoredResumeContent baseResume) {
        TailoredResumeContent tailored = tailoringPipeline.execute(job, baseResume);
        validationChain.validate(tailored);
        return tailored;
    }
}
