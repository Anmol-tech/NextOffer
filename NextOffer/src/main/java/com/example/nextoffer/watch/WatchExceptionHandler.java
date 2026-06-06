package com.example.nextoffer.watch;

import com.example.nextoffer.career.CareerPageFetchException;
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

    @ExceptionHandler(CareerPageFetchException.class)
    public ProblemDetail handleCareerPageFetch(CareerPageFetchException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Could not load career page");
        return problem;
    }

    @ExceptionHandler(WatchPollException.class)
    public ProblemDetail handlePollFailed(WatchPollException ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof CareerPageFetchException fetchException) {
            return handleCareerPageFetch(fetchException);
        }
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, friendlyPollMessage(ex));
        problem.setTitle("Could not check career page");
        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleBadRequest(IllegalArgumentException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Invalid request");
        return problem;
    }

    private static String friendlyPollMessage(WatchPollException ex) {
        String message = ex.getMessage();
        if (message != null && message.contains("404") && message.contains("Job not found")) {
            return """
                    The career page URL or platform type does not match a valid job board. \
                    Remove this watch and add it again with the correct platform and main careers URL.""";
        }
        return message != null ? message : "Career page check failed.";
    }
}
