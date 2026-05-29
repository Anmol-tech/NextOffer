package com.example.nextoffer.job;

import com.example.nextoffer.auth.AuthUserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class JobPostingService {

    private final JobPostingRepository jobPostingRepository;

    public JobPostingService(JobPostingRepository jobPostingRepository) {
        this.jobPostingRepository = jobPostingRepository;
    }

    @Transactional(readOnly = true)
    public List<JobPostingResponse> listForUser(AuthUserDetails principal) {
        return jobPostingRepository.findByUserIdOrderByFirstSeenAtDesc(principal.getUser().getId())
                .stream()
                .map(JobPostingResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public JobPostingResponse getForUser(Long id, AuthUserDetails principal) {
        JobPosting job = jobPostingRepository.findByIdAndUserId(id, principal.getUser().getId())
                .orElseThrow(() -> new JobPostingNotFoundException(id));
        return JobPostingResponse.from(job);
    }
}
