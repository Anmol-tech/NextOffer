package com.example.nextoffer.tracker;

import com.example.nextoffer.auth.AuthUserDetails;
import com.example.nextoffer.job.JobPostingResponse;
import com.example.nextoffer.tracker.dto.UpdateApplicationStatusRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
public class ApplicationTrackerController {

    private final ApplicationTrackerService applicationTrackerService;

    public ApplicationTrackerController(ApplicationTrackerService applicationTrackerService) {
        this.applicationTrackerService = applicationTrackerService;
    }

    @PatchMapping("/{id}/application-status")
    public JobPostingResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateApplicationStatusRequest request,
            @AuthenticationPrincipal AuthUserDetails principal) {
        return applicationTrackerService.updateStatus(id, principal, request.status());
    }
}
