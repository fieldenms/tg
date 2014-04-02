package ua.com.fielden.platform.algorithm.search;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * A generic implementation of the FIFO queue.
 * 
 * @author TG Team
 * 
 */
public final class FifoQueue<T> implements IQueue<T> {

    private final LinkedList<T> queue = new LinkedList<T>();

    public FifoQueue(final T... elements) {
        for (final T el : elements) {
            queue.add(el);
        }
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.unmodifiableList(queue).iterator();
    }

    @Override
    public T pop() {
        return queue.remove();
    }

    @Override
    public FifoQueue<T> push(final T el) {
        queue.add(el);
        return this;
    }

    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public boolean contains(final T el) {
        return queue.contains(el);
    }

}
