package com.example.nextoffer.resume;


/**
 * Composite — component interface for resume section tree.
 */
public interface ResumeSectionComponent {

    String getName();

    void accept(ResumeSectionVisitor visitor);

    default void add(ResumeSectionComponent child) {
        throw new UnsupportedOperationException("Leaf nodes cannot add children");
    }
}
