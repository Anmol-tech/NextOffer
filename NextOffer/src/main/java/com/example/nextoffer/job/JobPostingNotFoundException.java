package com.example.nextoffer.job;

public class JobPostingNotFoundException extends RuntimeException {

    public JobPostingNotFoundException(Long id) {
        super("Job posting not found: " + id);
    }
}
