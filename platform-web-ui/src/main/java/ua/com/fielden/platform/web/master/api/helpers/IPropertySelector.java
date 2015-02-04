package ua.com.fielden.platform.web.master.api.helpers;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 *
 * Provides a way to add a property of the designated entity type to the master being constructed.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IPropertySelector<T extends AbstractEntity<?>> {
    IWidgetSelector<T> addProp(final String propName);
}
