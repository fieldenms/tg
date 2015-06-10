package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.resultset.summary.IWithSummary;

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

    IWithSummary<T> addProp(final PropDef<?> propDef);

}
