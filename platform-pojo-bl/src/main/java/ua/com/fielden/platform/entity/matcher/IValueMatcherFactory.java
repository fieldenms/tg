package ua.com.fielden.platform.entity.matcher;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract for a factory to provide an appropriate {@link IValueMatcher} instance for a property belonging to a specified entity.
 *
 * @author TG Team
 *
 */
public interface IValueMatcherFactory {

    /**
     * Should return a value matcher associated with provided entity type and property.
     *
     * @param propertyOwnerEntityType
     * @param propertyName
     * @return
     */
    IValueMatcher<?> getValueMatcher(final Class<? extends AbstractEntity<?>> propertyOwnerEntityType, final String propertyName);
}
