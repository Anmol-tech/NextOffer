package com.example.nextoffer.job;

import com.example.nextoffer.auth.AuthUserDetails;
import com.example.nextoffer.resume.dto.TailoredResumeDetailResponse;
import com.example.nextoffer.resume.ResumeService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
public class JobResumeController {

    private final ResumeService resumeService;

    public JobResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping("/{jobId}/resumes/generate")
    @ResponseStatus(HttpStatus.CREATED)
    public TailoredResumeDetailResponse generate(
            @PathVariable Long jobId,
            @AuthenticationPrincipal AuthUserDetails principal) {
        return resumeService.generateForJob(jobId, principal);
    }
}
