package com.example.nextoffer.job;

import com.example.nextoffer.auth.AuthUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobPostingController {

    private final JobPostingService jobPostingService;

    public JobPostingController(JobPostingService jobPostingService) {
        this.jobPostingService = jobPostingService;
    }

    @GetMapping
    public List<JobPostingResponse> list(@AuthenticationPrincipal AuthUserDetails principal) {
        return jobPostingService.listForUser(principal);
    }

    @GetMapping("/{id}")
    public JobPostingResponse get(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthUserDetails principal) {
        return jobPostingService.getForUser(id, principal);
    }
}
