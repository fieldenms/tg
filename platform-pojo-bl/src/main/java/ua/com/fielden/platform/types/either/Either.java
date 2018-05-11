package ua.com.fielden.platform.types.either;

import static java.util.Objects.requireNonNull;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.utils.EntityUtils.equalsEx;

import java.util.function.Function;
import java.util.function.Supplier;

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

        throw failure("Attempt to convert Left to Right.");
    }

    public Left<L, R> asLeft() {
        if (isLeft()) {
            return (Left<L, R>) this;
        }

        throw failure("Attempt to convert Right to Left.");
    }

    /**
     * A right associative getter, returning the right value if {@code this} represents {@link Right}.
     * Otherwise, a value supplied by {@code supplier} is returned.
     *
     * @param supplierOfAlternative
     * @return
     */
    public <T extends R> R getOrElse(final Supplier<T> supplierOfAlternative) {
        requireNonNull(supplierOfAlternative);
        return isRight() ? asRight().value : supplierOfAlternative.get();
    }

    /**
     * A convenient method to get the right value if {@code this} represents
     * {@link Right}. Otherwise, left value is transformed in
     * {@link RuntimeException} and thrown.
     *
     * @param function
     *            used to transform left value in {@link RuntimeException}
     * @return
     */
    public R orElseThrow(final Function<? super L, ? extends RuntimeException> function) {
        requireNonNull(function);
        if (isRight()) {
            return asRight().value;
        } else {
            throw function.apply(asLeft().value);
        }
    }


    /**
     * Maps on the right value.
     *
     * @param f
     * @return
     */
    public <T> Either<L, T> map(final Function<? super R, ? extends T> f) {
        requireNonNull(f);
        return isRight() ? right(f.apply(asRight().value)) : left(asLeft().value);
    }

    /**
     * Flatmaps on the right value.
     *
     * @param f
     * @return
     */
    public <T> Either<? super L, T> flatMap(final Function<? super R, Either<? super L, T>> f) {
        requireNonNull(f);
        return isRight() ? f.apply(asRight().value) : left(asLeft().value);
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Either)) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        final Either<?,?> that = (Either<?,?>) obj;
        if (this.isLeft()) {
            return that.isLeft() && equalsEx(this.asLeft().value, that.asLeft().value);
        } else {
            return that.isRight() && equalsEx(this.asRight().value, that.asRight().value);
        }
    }

    @Override
    public int hashCode() {
        if (isLeft()) {
            return asLeft().value != null ? asLeft().value.hashCode() * 29 : 31;
        } else {
            return asRight().value != null ? asRight().value.hashCode() * 29 : 11;
        }
    }
}
