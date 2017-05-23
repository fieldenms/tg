package ua.com.fielden.platform.types.try_wrapper;

/**
 * A contract that describes a computation that may throw and exception
 * 
 * @author TG Team
 *
 * @param <T>
 */

@FunctionalInterface
public interface FailableComputation<T> {

    /**
     *
     * @return a value of type {@code T}
     * @throws Throwable if it fails
     */
    public T get() throws Exception;
}