package ua.com.fielden.platform.utils;

import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

final class LazySupplier<T> implements Lazy<T> {

    private Supplier<T> supplier;
    private T value;

    LazySupplier(final Supplier<T> supplier) {
        this.supplier = requireNonNull(supplier);
    }

    @Override
    public T get() {
        final T v;
        if (supplier == null) {
            v = value;
        } else {
            v = supplier.get();
            value = v;
            supplier = null;
        }
        return v;
    }

    @Override
    public void lambdasAreProhibited() {}

}
