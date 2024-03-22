package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;

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
     * Adds result-set column for {@code propName}, possibly in "dot-notation" form (e.g. "zone.sector.division").
     */
    @Deprecated
    IResultSetBuilder3Ordering<T> addProp(final String propName, final boolean presentByDefault);

    /**
     * Adds result-set column for {@code propName}, possibly in "dot-notation" form (e.g. "zone.sector.division").
     */
    @Deprecated
    default IResultSetBuilder3Ordering<T> addProp(final String propName) {
        return addProp(propName, true);
    }

    /**
     * Adds result-set column for {@code propName}, possibly in "dot-notation" form (e.g. Station_.zone().sector().division()).
     */
    default IResultSetBuilder3Ordering<T> addProp(final IConvertableToPath propName, final boolean presentByDefault) {
        return addProp(propName.toPath(), presentByDefault);
    }

    /**
     * Adds result-set column for {@code propName}, possibly in "dot-notation" form (e.g. Station_.zone().sector().division()).
     */
    default IResultSetBuilder3Ordering<T> addProp(final IConvertableToPath propName) {
        return addProp(propName.toPath());
    }

    /**
     * Adds editable result-set column for {@code propName}, possibly in "dot-notation" form (e.g. "zone.sector.division").
     */
    @Deprecated
    IResultSetBuilderWidgetSelector<T> addEditableProp(final String propName, final boolean presentByDefault);

    /**
     * Adds editable result-set column for {@code propName}, possibly in "dot-notation" form (e.g. "zone.sector.division").
     */
    @Deprecated
    default IResultSetBuilderWidgetSelector<T> addEditableProp(final String propName) {
        return addEditableProp(propName, true);
    }

    /**
     * Adds editable result-set column for {@code propName}, possibly in "dot-notation" form (e.g. Station_.zone().sector().division()).
     */
    default IResultSetBuilderWidgetSelector<T> addEditableProp(final IConvertableToPath propName, final boolean presentByDefault) {
        return addEditableProp(propName.toPath(), presentByDefault);
    }

    /**
     * Adds editable result-set column for {@code propName}, possibly in "dot-notation" form (e.g. Station_.zone().sector().division()).
     */
    default IResultSetBuilderWidgetSelector<T> addEditableProp(final IConvertableToPath propName) {
        return addEditableProp(propName.toPath());
    }

    IResultSetBuilder4aWidth<T> addProp(final PropDef<?> propDef, final boolean presentByDefault);

    default IResultSetBuilder4aWidth<T> addProp(final PropDef<?> propDef) {
        return addProp(propDef, true);
    }

}