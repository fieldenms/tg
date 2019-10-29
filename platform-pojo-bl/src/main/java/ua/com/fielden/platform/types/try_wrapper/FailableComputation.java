package ua.com.fielden.platform.types.try_wrapper;

/**
 * A contract that describes a computation that may throw an exception.
 *
 * @author TG Team
 *
 * @param <T>
 */

@FunctionalInterface
public interface FailableComputation<T> {

    /**
     * Executes a computation returning a value of type {@code T}.
     *
     * @return a value of type {@code T}
     * @throws Throwable
     *             if it fails
     */
    public T get() throws Exception;
}