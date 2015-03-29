package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 *
 * Provides a convenient abstraction for specifying the result set configuration for an entity centre.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IResultSetBuilder<T extends AbstractEntity<?>> {

    /**
     * Adds property of ant level to the result set.
     *
     * @param propName
     */
    IResultSetBuilder0Ordering<T> addProp(final String propName);

    IResultSetBuilder2WithPropAction<T> addProp(final PropDef<?> propDef);

}
