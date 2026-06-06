package com.example.nextoffer.tracker.dto;

import com.example.nextoffer.tracker.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateApplicationStatusRequest(@NotNull ApplicationStatus status) {
}
