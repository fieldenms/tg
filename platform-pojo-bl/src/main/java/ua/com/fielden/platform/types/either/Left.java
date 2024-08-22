package ua.com.fielden.platform.types.either;

/**
 * The left value of {@link Either}.
 * 
 * @author TG Team
 */
public record Left<L, R>(L value) implements Either<L, R> {
}
