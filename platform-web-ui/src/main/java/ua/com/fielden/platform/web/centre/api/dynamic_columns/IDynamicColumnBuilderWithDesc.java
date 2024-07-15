package ua.com.fielden.platform.web.centre.api.dynamic_columns;

/**
 * A contract to specify a tooltip for a column header.
 *
 * @author TG Team
 *
 */
public interface IDynamicColumnBuilderWithDesc extends IDynamicColumnBuilderWidth {

    IDynamicColumnBuilderWidth desc(final String desc);
}
