package com.example.nextoffer.resume;

import com.example.nextoffer.job.JobPostingDto;

import com.example.nextoffer.job.JobPostingDto;

/**
 * Facade — one call site for tailor + validate + render.
 */
public class ResumeTailoringFacade {

    private final AbstractTailoringPipeline tailoringPipeline;
    private final ResumeValidationHandler validationChain;
    private final LatexResumeRenderer renderer;

    public ResumeTailoringFacade() {
        this.tailoringPipeline = new OpenAiTailoringPipeline();
        ResumeValidationHandler fabrication = new NoFabricationValidationHandler();
        fabrication.linkWith(new AtsLengthValidationHandler());
        this.validationChain = fabrication;
        this.renderer = new LatexResumeRenderer(
                new ResumeTemplatePrototype("\\documentclass{article}", "\\end{document}")
        );
    }

    public byte[] tailorAndRender(JobPostingDto job, TailoredResumeContent baseResume) {
        TailoredResumeContent tailored = tailoringPipeline.execute(job, baseResume);
        validationChain.validate(tailored);
        return renderer.render(tailored);
    }

    public TailoredResumeContent quickBuild(JobPostingDto job) {
        return new TailoredResumeBuilder()
                .summary("Tailored for " + job.title())
                .addSkill("Java")
                .build();
    }
}
