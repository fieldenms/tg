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
public interface IResultSetBuilder2Properties<T extends AbstractEntity<?>> extends IResultSetBuilderDynamicProps<T>{

    /**
     * Adds property of ant level to the result set.
     *
     * @param propName
     */
    IResultSetBuilder3Ordering<T> addProp(final String propName);

    IResultSetBuilderWidgetSelector<T> addEditableProp(final String propName);

    IResultSetBuilder4aWidth<T> addProp(final PropDef<?> propDef);
}
