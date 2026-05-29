package com.example.nextoffer.resume.dto;

import com.example.nextoffer.resume.ResumeOutputStatus;
import com.example.nextoffer.resume.TailoredResume;

import java.time.Instant;

public record TailoredResumeResponse(
        Long id,
        Long jobPostingId,
        String jobTitle,
        String companyName,
        ResumeOutputStatus outputStatus,
        String summaryPreview,
        Instant createdAt
) {
    public static TailoredResumeResponse from(TailoredResume resume) {
        String preview = resume.getContentJson();
        if (preview != null && preview.length() > 120) {
            preview = preview.substring(0, 120) + "...";
        }
        return new TailoredResumeResponse(
                resume.getId(),
                resume.getJobPosting().getId(),
                resume.getJobPosting().getTitle(),
                resume.getJobPosting().getCompanyName(),
                resume.getOutputStatus(),
                preview,
                resume.getCreatedAt()
        );
    }
}
