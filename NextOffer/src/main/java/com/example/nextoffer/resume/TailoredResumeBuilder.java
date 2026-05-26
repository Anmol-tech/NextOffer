package com.example.nextoffer.resume;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder — constructs tailored resume content step by step.
 */
public class TailoredResumeBuilder {

    private String summary = "";
    private final List<String> skills = new ArrayList<>();
    private final List<String> experienceBullets = new ArrayList<>();

    public TailoredResumeBuilder summary(String summary) {
        this.summary = summary;
        return this;
    }

    public TailoredResumeBuilder addSkill(String skill) {
        skills.add(skill);
        return this;
    }

    public TailoredResumeBuilder addExperienceBullet(String bullet) {
        experienceBullets.add(bullet);
        return this;
    }

    public TailoredResumeContent build() {
        return new TailoredResumeContent(summary, List.copyOf(skills), List.copyOf(experienceBullets));
    }
}
