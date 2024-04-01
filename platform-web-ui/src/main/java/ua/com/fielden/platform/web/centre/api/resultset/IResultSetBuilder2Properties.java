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
     * Adds a property to an Entity Centre result set. Supports dot-notation (e.g., {@code Station_.zone().sector().division()}).
     */
    IResultSetBuilder3Ordering<T> addProp(final CharSequence propName);

    /**
     * Adds a property to an Entity Centre result set.
     * 
     * @param presentByDefault -- indicates whether the property should be present in the result set by default; users can add/remove the property using AZ (Customise Columns) action
     */
    IResultSetBuilder3Ordering<T> addProp(final CharSequence prop, final boolean presentByDefault);

    /**
     * Adds editable result-set column for a property, possibly in "dot-notation" form (e.g. "zone.sector.division").
     */
    IResultSetBuilderWidgetSelector<T> addEditableProp(final CharSequence propName);

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
