package com.example.nextoffer.watch;

import com.example.nextoffer.job.JobPostingNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class WatchExceptionHandler {

    @ExceptionHandler(WatchNotFoundException.class)
    public ProblemDetail handleWatchNotFound(WatchNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Company watch not found");
        return problem;
    }

    @ExceptionHandler(JobPostingNotFoundException.class)
    public ProblemDetail handleJobNotFound(JobPostingNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Job posting not found");
        return problem;
    }

    @ExceptionHandler(WatchPollException.class)
    public ProblemDetail handlePollFailed(WatchPollException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, ex.getMessage());
        problem.setTitle("Career page poll failed");
        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleBadRequest(IllegalArgumentException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Invalid request");
        return problem;
    }
}
