package ua.com.fielden.platform.processors.utils;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public final class OptionalUtils {

    public static <T, R> R ifEmptyOrMap(Optional<T> optional, Supplier<? extends R> ifEmpty, Function<? super T, R> mapper) {
        return optional.isEmpty() ? ifEmpty.get() : mapper.apply(optional.get());
    }

    private OptionalUtils() {}

}
