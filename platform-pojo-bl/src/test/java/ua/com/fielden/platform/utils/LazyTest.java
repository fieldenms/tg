package ua.com.fielden.platform.utils;

import jakarta.inject.Provider;
import org.junit.Test;

import java.util.List;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static ua.com.fielden.platform.utils.Lazy.lazyProvider;
import static ua.com.fielden.platform.utils.Lazy.lazySupplier;

public class LazyTest {

    @Test
    public void lazySupplier_calls_the_supplier_only_once_and_remembers_the_value() {
        final Supplier<List<Object>> supplier = () -> List.of(1);
        final Lazy<List<Object>> lazyValue = lazySupplier(supplier);

        final var value = lazyValue.get();
        assertEquals(List.of(1), value);
        assertSame(value, lazyValue.get());
    }

    @Test
    public void lazyProvider_calls_the_provider_only_once_and_remembers_the_value() {
        final Provider<List<Object>> provider = () -> List.of(1);
        final Lazy<List<Object>> lazyValue = lazyProvider(provider);

        final var value = lazyValue.get();
        assertEquals(List.of(1), value);
        assertSame(value, lazyValue.get());
    }

}
