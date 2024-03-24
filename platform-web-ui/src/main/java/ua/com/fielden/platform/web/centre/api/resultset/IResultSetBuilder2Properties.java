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
     * Adds {@code propName} to an Entity Centre result set. Supports a dot-notation form (e.g., "zone.sector.division").
     * <p>
     * This method is deprecated, use {@link #addProp(IConvertableToPath)} instead.
     */
    @Deprecated
    IResultSetBuilder3Ordering<T> addProp(final String propName);

    /**
     * Adds {@code prop} to an Entity Centre result set. Supports a dot-notation form (e.g., Station_.zone().sector().division()).
     */
    default IResultSetBuilder3Ordering<T> addProp(final IConvertableToPath prop) {
        return addProp(prop, true);
    }

    /**
     * Adds property {@code prop} to an Entity Centre result set.
     * 
     * @param presentByDefault -- indicates whether the property should be present in the result set by default; users can add/remove the property using AZ (Customise Columns) action
     */
    IResultSetBuilder3Ordering<T> addProp(final IConvertableToPath prop, final boolean presentByDefault);

    /**
     * Adds editable result-set column for {@code propName}, possibly in "dot-notation" form (e.g. "zone.sector.division").
     * <p>
     * This method is deprecated, use {@link #addEditableProp(IConvertableToPath)} instead.
     */
    @Deprecated
    IResultSetBuilderWidgetSelector<T> addEditableProp(final String propName);

    /**
     * Adds editable result-set column for {@code propName}, possibly in "dot-notation" form (e.g. Station_.zone().sector().division()).
     */
    default IResultSetBuilderWidgetSelector<T> addEditableProp(final IConvertableToPath propName) {
        return addEditableProp(propName.toPath());
    }

    /**
     * Adds a custom column to an Entity Centre result set, using {@link PropDef} definition.
     * 
     * @param presentByDefault -- indicates whether the property should be present in the result set by default; users can add/remove the property using AZ (Customise Columns) action.
     */
    IResultSetBuilder4aWidth<T> addProp(final PropDef<?> propDef, final boolean presentByDefault);

    /**
     * Adds custom result-set column using {@link PropDef} definition.
     */
    default IResultSetBuilder4aWidth<T> addProp(final PropDef<?> propDef) {
        return addProp(propDef, true);
    }

}