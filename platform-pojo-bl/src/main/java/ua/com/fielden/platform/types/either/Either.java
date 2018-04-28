package ua.com.fielden.platform.types.either;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import ua.com.fielden.platform.error.Result;

/**
 * A container type that represent one of two possible values of some computed.
 * One of the values that is often referred to as the <code>Right</code> value has the semantics of being correct.
 * Another value, which is often referred to as the <code>Left</code> value, has the semantics of being invalid or alternative to the correct value. 
 * 
 * @author TG Team
 *
 * @param <E>
 * @param <V>
 */
public abstract class Either<L, R> {

    /**
     * Convenient factory method.
     * 
     * @param value
     * @return
     */
    public static <L, R> Right<L, R> right(final R value) {
        return new Right<>(value);
    }

    /**
     * Convenient factory method.
     * 
     * @param value
     * @return
     */
    public static <L, R> Left<L, R> left(final L value) {
        return new Left<>(value);
    }

    public boolean isLeft() {
        return this instanceof Left;
    }

    public boolean isRight() {
        return this instanceof Right;
    }

    public Right<L, R> asRight() {
        if (isRight()) {
            return (Right<L, R>) this;
        }

        throw Result.failure("Attempty to covernt Left to Right.");
    }

    public Left<L, R> asLeft() {
        if (isLeft()) {
            return (Left<L, R>) this;
        }

        throw Result.failure("Attempty to covernt Right to Left.");
    }

    /**
     * Maps on the right value.
     *
     * @param f
     * @return
     */
    public <T> Either<L, T> map(final Function<? super R, ? extends T> f) {
        Objects.requireNonNull(f);
        if (isRight()) {
            final R value = ((Right<L, R>) this).value;
            return right(f.apply(value));
        } else {
            final L value = ((Left<L, R>) this).value;
            return left(value);
        }
    }
}
