package com.example.nextoffer.resume.dto;

import jakarta.validation.constraints.NotBlank;

public record SaveBaseResumeRequest(@NotBlank String rawText) {
}
