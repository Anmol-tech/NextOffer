package com.example.nextoffer.resume;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ResumeExceptionHandler {

    @ExceptionHandler(BaseResumeNotFoundException.class)
    public ProblemDetail handleBaseMissing(BaseResumeNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Base resume required");
        return problem;
    }

    @ExceptionHandler({TailoredResumeNotFoundException.class, ResumeFileNotFoundException.class})
    public ProblemDetail handleNotFound(RuntimeException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Resume not found");
        return problem;
    }

    @ExceptionHandler(ResumeGenerationException.class)
    public ProblemDetail handleGeneration(ResumeGenerationException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, ex.getMessage());
        problem.setTitle("Resume generation failed");
        return problem;
    }
}
