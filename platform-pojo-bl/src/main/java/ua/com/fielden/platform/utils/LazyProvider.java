package ua.com.fielden.platform.utils;

import jakarta.inject.Provider;

import static java.util.Objects.requireNonNull;

final class LazyProvider<T> implements Lazy<T> {

    private Provider<T> provider;
    private T value;

    LazyProvider(final Provider<T> provider) {
        this.provider = requireNonNull(provider);
    }

    @Override
    public T get() {
        final T v;
        if (provider == null) {
            v = value;
        } else {
            v = provider.get();
            value = v;
            provider = null;
        }
        return v;
    }

    @Override
    public void lambdasAreProhibited() {}

}
