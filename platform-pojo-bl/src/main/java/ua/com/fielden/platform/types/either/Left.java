package ua.com.fielden.platform.types.either;

/**
 * The left value of {@link Either}.
 * 
 * @author TG Team
 *
 * @param <L>
 */
public final class Left<L, R> extends Either<L, R> {
    public final L value;

    public Left(final L v) {
        value = v;
    }
}
