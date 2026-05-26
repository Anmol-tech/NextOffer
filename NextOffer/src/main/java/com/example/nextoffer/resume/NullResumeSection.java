package com.example.nextoffer.resume;


/**
 * Null Object — safe stand-in when a resume section is absent.
 */
public class NullResumeSection implements ResumeSectionComponent {

    public static final NullResumeSection INSTANCE = new NullResumeSection();

    private NullResumeSection() {
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public void accept(ResumeSectionVisitor visitor) {
        // no-op
    }
}
