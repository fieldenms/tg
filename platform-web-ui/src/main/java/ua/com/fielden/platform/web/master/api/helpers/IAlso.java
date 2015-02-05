package ua.com.fielden.platform.web.master.api.helpers;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * This is an interface to make the Entity Master API more fluent.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IAlso<T extends AbstractEntity<?>> {
    IPropertySelector<T> also();
}
