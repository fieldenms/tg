package ua.com.fielden.platform.web.centre.api.dynamic_columns;

/**
 * A contract that adds a new dynamic column.
 *
 * @author TG Team
 *
 */
public interface IDynamicColumnBuilderAddProp {

    IDynamicColumnBuilderWithTitle addColumn(final String keyPropValue);
}
