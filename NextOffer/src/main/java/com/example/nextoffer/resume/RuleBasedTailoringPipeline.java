package com.example.nextoffer.resume;

import com.example.nextoffer.job.JobPostingDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RuleBasedTailoringPipeline extends AbstractTailoringPipeline {

    @Override
    protected TailoredResumeContent reorderSections(JobPostingDto job, TailoredResumeContent content) {
        Set<String> keywords = extractKeywords(job.title() + " " + safe(job.description()));

        List<String> rankedBullets = content.experienceBullets().stream()
                .sorted(Comparator.comparingInt(bullet -> -scoreBullet(bullet, keywords)))
                .toList();

        List<String> rankedSkills = content.skills().stream()
                .sorted(Comparator.comparingInt(skill -> -scoreBullet(skill, keywords)))
                .toList();

        String summary = content.summary().isBlank()
                ? "Candidate targeting " + job.title() + " at " + job.companyName() + "."
                : content.summary();

        TailoredResumeBuilder builder = new TailoredResumeBuilder().summary(summary);
        rankedSkills.forEach(builder::addSkill);
        rankedBullets.forEach(builder::addExperienceBullet);
        return builder.build();
    }

    private int scoreBullet(String text, Set<String> keywords) {
        String lower = text.toLowerCase(Locale.ROOT);
        int score = 0;
        for (String keyword : keywords) {
            if (lower.contains(keyword)) {
                score++;
            }
        }
        return score;
    }

    private Set<String> extractKeywords(String text) {
        return Arrays.stream(text.toLowerCase(Locale.ROOT).split("[^a-z0-9+#]+"))
                .filter(token -> token.length() >= 3)
                .collect(Collectors.toSet());
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
