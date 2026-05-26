package com.example.nextoffer.resume;


public class ResumeSectionLeaf implements ResumeSectionComponent {

    private final String name;
    private final String content;

    public ResumeSectionLeaf(String name, String content) {
        this.name = name;
        this.content = content;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    @Override
    public void accept(ResumeSectionVisitor visitor) {
        visitor.visitLeaf(this);
    }
}
