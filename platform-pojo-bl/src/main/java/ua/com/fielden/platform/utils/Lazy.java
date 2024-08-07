package ua.com.fielden.platform.utils;

import jakarta.inject.Provider;

import java.util.function.Supplier;

/**
 * Represents a lazy computation that is performed only once and remembers the result afterwards.
 *
 * @param <T>  type of computation's result
 */
public interface Lazy<T> extends Supplier<T>, Provider<T> {

    T get();

    /**
     * This method exists solely to prevent this interface from being used as a result of a lambda expression.
     * That is because such a lambda expression will lack the <i>memoization</i> property, and will act as an ordinary
     * {@link Supplier} instead.
     */
    void lambdasAreProhibited();

    static <T> Lazy<T> lazyS(Supplier<T> supplier) {
        return new LazySupplier<>(supplier);
    }

    static <T> Lazy<T> lazyP(Provider<T> provider) {
        return new LazyProvider<>(provider);
    }

}
