package com.example.nextoffer.resume.dto;

import com.example.nextoffer.resume.BaseResume;
import com.example.nextoffer.resume.ResumeSourceFormat;

import java.time.Instant;

public record BaseResumeResponse(
        Long id,
        String rawText,
        Instant updatedAt,
        ResumeSourceFormat sourceFormat
) {

    public static BaseResumeResponse from(BaseResume resume, ResumeSourceFormat sourceFormat) {
        return new BaseResumeResponse(resume.getId(), resume.getRawText(), resume.getUpdatedAt(), sourceFormat);
    }
}
