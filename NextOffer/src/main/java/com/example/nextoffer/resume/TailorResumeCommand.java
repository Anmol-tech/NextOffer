package com.example.nextoffer.resume;

import com.example.nextoffer.job.JobPostingDto;

import com.example.nextoffer.job.JobPostingDto;

public class TailorResumeCommand implements ResumeCommand {

    private final ResumeTailoringFacade facade;
    private final JobPostingDto job;
    private final TailoredResumeContent baseResume;
    private byte[] lastPdf;

    public TailorResumeCommand(ResumeTailoringFacade facade, JobPostingDto job, TailoredResumeContent baseResume) {
        this.facade = facade;
        this.job = job;
        this.baseResume = baseResume;
    }

    @Override
    public void execute() {
        lastPdf = facade.tailorAndRender(job, baseResume);
    }

    @Override
    public void undo() {
        lastPdf = null;
    }

    public byte[] getLastPdf() {
        return lastPdf;
    }
}
