package ua.com.fielden.platform.processors.metamodel;

import java.util.stream.IntStream;

/**
 * A contract for constructs that can be converted to a dot-noted property path.
 * 
 * @author TG Team
 *
 */
public interface IConvertableToPath extends CharSequence {

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
    default CharSequence subSequence(int start, int end) {
        return toPath().subSequence(start, end);
    }

    @Override
    default IntStream chars() {
        return toPath().chars();
    }

    @Override
    default IntStream codePoints() {
        return toPath().codePoints();
    }

}
