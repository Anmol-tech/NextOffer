package com.example.nextoffer.resume;


import java.util.ArrayList;
import java.util.List;

public class ResumeSectionGroup implements ResumeSectionComponent {

    private final String name;
    private final List<ResumeSectionComponent> children = new ArrayList<>();

    public ResumeSectionGroup(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void add(ResumeSectionComponent child) {
        children.add(child);
    }

    public List<ResumeSectionComponent> getChildren() {
        return List.copyOf(children);
    }

    @Override
    public void accept(ResumeSectionVisitor visitor) {
        visitor.visitGroup(this);
        children.forEach(child -> child.accept(visitor));
    }
}
