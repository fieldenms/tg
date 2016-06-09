package ua.com.fielden.platform.basic;

/**
 * This is a marker contract that declares an intention by a value matcher that implements it, to search values by using <code>desc</desc> property.
 * <p>
 * Its use is mainly intended for early runtime type checking to ensure that UI property editors, which declare their intention to search by entity description, are actually provided with an appropriate value matcher.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IValueMatcherByDesc<T> extends IValueMatcher<T> {
}
