package ua.com.fielden.platform.web.centre.api.dynamicprops;

/**
 * A contract that specifies the width or min-width of column.
 *
 * @author TG Team
 *
 */
public interface IDynamicPropBuilderWidth  extends IDynamicPropBuilderDone{

    IDynamicPropBuilderDone width(int width);

    IDynamicPropBuilderDone minWidth(int width);
}
