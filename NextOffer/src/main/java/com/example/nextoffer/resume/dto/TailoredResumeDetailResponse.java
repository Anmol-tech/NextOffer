package com.example.nextoffer.resume.dto;

import com.example.nextoffer.resume.TailoredResume;
import com.example.nextoffer.resume.TailoredResumeContent;
import com.example.nextoffer.resume.ResumeSourceFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;

public record TailoredResumeDetailResponse(
        Long id,
        Long jobPostingId,
        String jobTitle,
        String companyName,
        TailoredResumeContent content,
        String latexContent,
        String sourceFormat,
        String outputStatus,
        boolean pdfAvailable,
        Instant createdAt
) {
    public static TailoredResumeDetailResponse from(TailoredResume resume, ObjectMapper mapper, ResumeSourceFormat baseFormat) {
        TailoredResumeContent content;
        try {
            content = mapper.readValue(resume.getContentJson(), TailoredResumeContent.class);
        } catch (JsonProcessingException ex) {
            content = new TailoredResumeContent("", java.util.List.of(), java.util.List.of());
        }
        return new TailoredResumeDetailResponse(
                resume.getId(),
                resume.getJobPosting().getId(),
                resume.getJobPosting().getTitle(),
                resume.getJobPosting().getCompanyName(),
                content,
                resume.getLatexContent(),
                baseFormat.name(),
                resume.getOutputStatus().name(),
                resume.getPdfStoragePath() != null,
                resume.getCreatedAt()
        );
    }
}
