package com.example.nextoffer.resume;


/**
 * Bridge — abstraction for rendering tailored content to an output format.
 */
public interface ResumeRenderer {

    byte[] render(TailoredResumeContent content);
}
