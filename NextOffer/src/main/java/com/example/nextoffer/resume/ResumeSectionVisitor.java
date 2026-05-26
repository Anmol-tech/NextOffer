package com.example.nextoffer.resume;


/**
 * Visitor — operations over resume section tree without modifying section classes.
 */
public interface ResumeSectionVisitor {

    void visitLeaf(ResumeSectionLeaf leaf);

    void visitGroup(ResumeSectionGroup group);
}
