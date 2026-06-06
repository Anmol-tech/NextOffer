package com.example.nextoffer.tracker;

import com.example.nextoffer.auth.AuthUserDetails;
import com.example.nextoffer.job.JobPosting;
import com.example.nextoffer.job.JobPostingNotFoundException;
import com.example.nextoffer.job.JobPostingRepository;
import com.example.nextoffer.job.JobPostingResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class ApplicationTrackerService {

    private final JobPostingRepository jobPostingRepository;

    public ApplicationTrackerService(JobPostingRepository jobPostingRepository) {
        this.jobPostingRepository = jobPostingRepository;
    }

    @Transactional
    public JobPostingResponse updateStatus(
            Long jobId,
            AuthUserDetails principal,
            ApplicationStatus targetStatus) {
        JobPosting job = jobPostingRepository.findByIdAndUserId(jobId, principal.getUser().getId())
                .orElseThrow(() -> new JobPostingNotFoundException(jobId));

        ApplicationTrackerContext context = ApplicationTrackerContext.fromStatus(job.getApplicationStatus());
        context.transitionTo(targetStatus);

        job.setApplicationStatus(context.getStatus());
        job.setStatusUpdatedAt(Instant.now());
        return JobPostingResponse.from(jobPostingRepository.save(job));
    }
}
