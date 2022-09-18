package ua.com.fielden.platform.types.try_wrapper;

import java.util.Objects;

/**
 * A contract that describes a consumer that may throw an exception.
 *
 * @author TG Team
 *
 * @param <T>
 */

@FunctionalInterface
public interface ThrowableConsumer<T> {

    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     * @throws Throwable if it fails
     */
    void accept(T t) throws Throwable;

    /**
     * Returns a composed {@code ThrowableConsumer} that performs, in sequence, this
     * operation followed by the {@code after} operation. If performing either
     * operation throws an exception, it is relayed to the caller of the
     * composed operation.  If performing this operation throws an exception,
     * the {@code after} operation will not be performed.
     *
     * @param after the operation to perform after this operation
     * @return a composed {@code FailableConsumer} that performs in sequence this
     * operation followed by the {@code after} operation
     * @throws NullPointerException if {@code after} is null
     */
    default ThrowableConsumer<T> andThen(final ThrowableConsumer<? super T> after) throws Throwable {
        Objects.requireNonNull(after);
        return (T t) -> { accept(t); after.accept(t); };
    }

}