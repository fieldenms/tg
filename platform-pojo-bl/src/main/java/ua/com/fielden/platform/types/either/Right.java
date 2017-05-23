package ua.com.fielden.platform.types.either;

/**
 * The right value of {@link Either}.
 * 
 * @author TG Team
 *
 * @param <R>
 */
public final class Right<L, R> extends Either<L, R> {
    public final R value;

    public Right(final R v) {
        value = v;
    }
}