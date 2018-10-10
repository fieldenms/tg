package ua.com.fielden.platform.basic;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 *
 * A value matcher contract that requires context in a form of an entity, which is used by the matcher to narrow down the value search.
 * It is envisaged that the context is always an entity that is the owner of the property for which the values are being searched by the value matcher.
 *
 * @author TG Team
 *
 * @param <T>
 * @param <C>
 */
public interface IContextValueMatcher<T, C extends AbstractEntity<?>> extends IValueMatcher<T> {

    /**
     * A setter method to provide a matching context.
     * The context should be set just before invoking {@link #findMatches(String)} or {@link #findMatchesWithModel(String)} (i.e. before the actual search takes place).
     *
     * @param entity
     * @return
     */
    IContextValueMatcher<T, C> setContext(final C entity);
}
