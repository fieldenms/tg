package ua.com.fielden.platform.types.either;

/**
 * The right value of {@link Either}.
 * 
 * @author TG Team
 */
public record Right<L, R>(R value) implements Either<L, R> {
}