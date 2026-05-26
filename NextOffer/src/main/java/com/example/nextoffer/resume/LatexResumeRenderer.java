package com.example.nextoffer.resume;


/**
 * Bridge — concrete renderer using LaTeX → PDF pipeline.
 */
public class LatexResumeRenderer implements ResumeRenderer {

    private final ResumeTemplatePrototype templatePrototype;

    public LatexResumeRenderer(ResumeTemplatePrototype templatePrototype) {
        this.templatePrototype = templatePrototype;
    }

    @Override
    public byte[] render(TailoredResumeContent content) {
        String body = content.summary() + "\n" + String.join("\n", content.experienceBullets());
        String latex = templatePrototype.clone().renderWithContent(body);
        return latex.getBytes();
    }
}
