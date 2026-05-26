package com.example.nextoffer.resume;

/**
 * Prototype — clone a base LaTeX template for each job-specific resume.
 */
public class ResumeTemplatePrototype implements Cloneable {

    private final String latexHeader;
    private final String sectionSlots;

    public ResumeTemplatePrototype(String latexHeader, String sectionSlots) {
        this.latexHeader = latexHeader;
        this.sectionSlots = sectionSlots;
    }

    @Override
    public ResumeTemplatePrototype clone() {
        try {
            return (ResumeTemplatePrototype) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("Clone failed", e);
        }
    }

    public String renderWithContent(String body) {
        return latexHeader + "\n" + body + "\n" + sectionSlots;
    }
}
