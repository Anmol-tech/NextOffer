package com.example.nextoffer.job;

import com.example.nextoffer.job.JobPostingDto;

public class MatchScoreJobDecorator extends JobPostingDecorator {

    private final int matchPercent;

    public MatchScoreJobDecorator(JobPostingDto delegate, int matchPercent) {
        super(delegate);
        this.matchPercent = matchPercent;
    }

    @Override
    public String displayTitle() {
        return delegate.title() + " (" + matchPercent + "% match)";
    }

    public int getMatchPercent() {
        return matchPercent;
    }
}
