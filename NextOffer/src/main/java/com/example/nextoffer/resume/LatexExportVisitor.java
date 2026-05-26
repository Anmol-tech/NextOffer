package com.example.nextoffer.resume;


public class LatexExportVisitor implements ResumeSectionVisitor {

    private final StringBuilder latex = new StringBuilder();

    @Override
    public void visitLeaf(ResumeSectionLeaf leaf) {
        latex.append("\\section{").append(leaf.getName()).append("}\n");
        latex.append(leaf.getContent()).append("\n");
    }

    @Override
    public void visitGroup(ResumeSectionGroup group) {
        latex.append("% group: ").append(group.getName()).append("\n");
    }

    public String getLatex() {
        return latex.toString();
    }
}
