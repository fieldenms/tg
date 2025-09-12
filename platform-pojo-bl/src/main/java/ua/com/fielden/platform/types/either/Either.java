package ua.com.fielden.platform.types.either;

import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static ua.com.fielden.platform.error.Result.failure;

/**
 * A container type that represent one of two possible values of some computed.
 * One of the values that is often referred to as the {@link Right} value has the semantics of being correct.
 * Another value, which is often referred to as the {@link Left} value, which usually has the semantics of being invalid or alternative to the correct value.
 *
 * @author TG Team
 *
 * @param <L>
 * @param <R>
 */
public sealed interface Either<L, R> permits Left, Right {

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

    default boolean isLeft() {
        return this instanceof Left;
    }

    default boolean isRight() {
        return this instanceof Right;
    }

    default Right<L, R> asRight() {
        if (isRight()) {
            return (Right<L, R>) this;
        }

        throw failure("Attempt to convert Left to Right.");
    }

    default Left<L, R> asLeft() {
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
    default <T extends R> R getOrElse(final Supplier<T> supplierOfAlternative) {
        requireNonNull(supplierOfAlternative);
        return isRight() ? asRight().value() : supplierOfAlternative.get();
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
    default R orElseThrow(final Function<? super L, ? extends RuntimeException> function) {
        requireNonNull(function);
        if (isRight()) {
            return asRight().value();
        } else {
            throw function.apply(asLeft().value());
        }
    }

    /**
     * Maps on the right value.
     *
     * @param f
     * @return
     */
    default <T> Either<L, T> map(final Function<? super R, ? extends T> f) {
        requireNonNull(f);
        return isRight() ? right(f.apply(asRight().value())) : left(asLeft().value());
    }

    /**
     * Flatmaps on the right value.
     *
     * @param f
     * @return
     */
    default <T> Either<? super L, T> flatMap(final Function<? super R, Either<? super L, T>> f) {
        requireNonNull(f);
        return isRight() ? f.apply(asRight().value()) : left(asLeft().value());
    }

    /**
     * Applies {@code leftF} if this is a {@link Left} or {@code rightF} if this is a {@link Right}.
     *
     * @param leftF
     * @param rightF
     * @return
     * @param <T>
     */
    default <T> T fold(
            final Function<? super L, ? extends T> leftF,
            final Function<? super R, ? extends T> rightF)
    {
        requireNonNull(leftF);
        requireNonNull(rightF);
        return isLeft() ? leftF.apply(asLeft().value()) : rightF.apply(asRight().value());
    }

}
