package ua.com.fielden.platform.web.centre.api.dynamic_columns;

/**
 * A contract to specify the width or min-width of a column.
 *
 * @author TG Team
 *
 */
public interface IDynamicColumnBuilderWidth  extends IDynamicColumnBuilderAddPropWithDone{

    IDynamicColumnBuilderAddPropWithDone width(final int width);

    IDynamicColumnBuilderAddPropWithDone minWidth(final int width);
}
