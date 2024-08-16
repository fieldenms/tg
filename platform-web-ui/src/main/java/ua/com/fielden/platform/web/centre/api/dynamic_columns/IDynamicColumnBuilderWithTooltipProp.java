package ua.com.fielden.platform.web.centre.api.dynamic_columns;

/**
 * A contract to specify the name of a property that holds a tooltip for a column value
 *
 * @author TG Team
 *
 */
public interface IDynamicColumnBuilderWithTooltipProp extends IDynamicColumnBuilderAddPropWithDone{

    IDynamicColumnBuilderAddPropWithDone withTooltipProp(final CharSequence tooltipProp);

}
