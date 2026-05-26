package com.example.nextoffer.resume;


public class AtsLengthValidationHandler extends ResumeValidationHandler {

    private static final int MAX_BULLETS = 50;

    @Override
    protected void doValidate(TailoredResumeContent content) {
        if (content.experienceBullets().size() > MAX_BULLETS) {
            throw new IllegalArgumentException("Too many bullets for ATS-friendly layout");
        }
    }
}
