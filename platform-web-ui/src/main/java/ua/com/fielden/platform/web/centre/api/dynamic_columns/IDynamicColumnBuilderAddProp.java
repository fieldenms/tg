package ua.com.fielden.platform.web.centre.api.dynamic_columns;

/**
 * A contract that adds a new dynamic column with a specific group property value.
 *
 * @author TG Team
 *
 */
public interface IDynamicColumnBuilderAddProp {

    IDynamicColumnBuilderWithTitle addColumn(final String groupPropValue);
}
