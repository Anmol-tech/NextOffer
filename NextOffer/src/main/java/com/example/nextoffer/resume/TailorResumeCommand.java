package com.example.nextoffer.resume;

import com.example.nextoffer.job.JobPostingDto;

import java.nio.charset.StandardCharsets;

public class TailorResumeCommand implements ResumeCommand {

    private final ResumeTailoringFacade facade;
    private final JobPostingDto job;
    private final TailoredResumeContent baseResume;
    private byte[] lastOutput;

    public TailorResumeCommand(ResumeTailoringFacade facade, JobPostingDto job, TailoredResumeContent baseResume) {
        this.facade = facade;
        this.job = job;
        this.baseResume = baseResume;
    }

    @Override
    public void execute() {
        TailoredResumeContent tailored = facade.tailor(job, baseResume);
        lastOutput = tailored.summary().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void undo() {
        lastOutput = null;
    }

    public byte[] getLastOutput() {
        return lastOutput;
    }
}
