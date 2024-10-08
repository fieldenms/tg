package ua.com.fielden.platform.text.commonmark;

import org.commonmark.node.Node;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterates over all direct children of a node, without descending further.
 *
 * @see CommonMark#childrenIterator(Node)
 */
public final class ChildrenIterator implements Iterator<Node> {

    private Node nextChild;

    ChildrenIterator(final Node parent) {
        this.nextChild = parent.getFirstChild();
    }

    @Override
    public boolean hasNext() {
        return nextChild != null;
    }

    @Override
    public Node next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        final Node next = nextChild;
        nextChild = nextChild.getNext();
        return next;
    }

}
