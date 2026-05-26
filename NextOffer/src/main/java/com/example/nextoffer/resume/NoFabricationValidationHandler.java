package com.example.nextoffer.resume;


public class NoFabricationValidationHandler extends ResumeValidationHandler {

    @Override
    protected void doValidate(TailoredResumeContent content) {
        for (String bullet : content.experienceBullets()) {
            if (bullet.toLowerCase().contains("fabricated")) {
                throw new IllegalArgumentException("Resume contains unverified experience");
            }
        }
    }
}
