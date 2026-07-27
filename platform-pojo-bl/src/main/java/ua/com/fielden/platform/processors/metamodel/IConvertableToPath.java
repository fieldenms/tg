package ua.com.fielden.platform.processors.metamodel;

import jakarta.annotation.Nonnull;

import java.util.stream.IntStream;

/**
 * A contract for constructs that can be converted to a dot-noted property path.
 * 
 * @author TG Team
 *
 */
public interface IConvertableToPath extends CharSequence {

    @Nonnull
    String toPath();

    @Override
    default int length() {
        return toPath().length();
    }

    @Override
    default char charAt(int index) {
        return toPath().charAt(index);
    }

    @Override
    default boolean isEmpty() {
        return toPath().isEmpty();
    }

    @Override
    @Nonnull
    default CharSequence subSequence(int start, int end) {
        return toPath().subSequence(start, end);
    }

    @Override
    @Nonnull
    default IntStream chars() {
        return toPath().chars();
    }

    @Override
    @Nonnull
    default IntStream codePoints() {
        return toPath().codePoints();
    }

}
