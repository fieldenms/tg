package ua.com.fielden.platform.web.centre.api.dynamic_columns;

/**
 * A contract to specify the width or min-width of a column.
 *
 * @author TG Team
 *
 */
public interface IDynamicColumnBuilderWidth  extends IDynamicColumnBuilderDone{

    IDynamicColumnBuilderDone width(final int width);

    IDynamicColumnBuilderDone minWidth(final int width);
}
