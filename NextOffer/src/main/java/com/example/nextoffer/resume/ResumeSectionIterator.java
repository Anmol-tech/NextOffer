package com.example.nextoffer.resume;


import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterator — depth-first traversal of resume section composite tree.
 */
public class ResumeSectionIterator implements Iterator<ResumeSectionComponent> {

    private final Deque<ResumeSectionComponent> stack = new ArrayDeque<>();

    public ResumeSectionIterator(ResumeSectionGroup root) {
        pushChildren(root);
    }

    private void pushChildren(ResumeSectionGroup group) {
        var children = group.getChildren();
        for (int i = children.size() - 1; i >= 0; i--) {
            stack.push(children.get(i));
        }
    }

    @Override
    public boolean hasNext() {
        return !stack.isEmpty();
    }

    @Override
    public ResumeSectionComponent next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        ResumeSectionComponent current = stack.pop();
        if (current instanceof ResumeSectionGroup group) {
            pushChildren(group);
        }
        return current;
    }
}
