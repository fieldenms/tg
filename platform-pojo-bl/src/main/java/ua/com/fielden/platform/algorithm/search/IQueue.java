package ua.com.fielden.platform.algorithm.search;

/**
 * Queue contract.
 * 
 * @author TG Team
 * 
 * @param <T>
 */
public interface IQueue<T> extends Iterable<T> {
    T pop();

    IQueue<T> push(final T el);

    boolean isEmpty();

    boolean contains(final T el);
}
