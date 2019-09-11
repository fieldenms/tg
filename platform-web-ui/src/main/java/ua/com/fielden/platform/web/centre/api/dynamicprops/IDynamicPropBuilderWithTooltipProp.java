package ua.com.fielden.platform.web.centre.api.dynamicprops;

/**
 * A contract that specifies the name of property that holds the tooltip for column value
 *
 * @author TG Team
 *
 */
public interface IDynamicPropBuilderWithTooltipProp extends IDynamicPropBuilderAddProp, IDynamicPropBuilderDone{

    IDynamicPropBuilderAddProp withTooltipProp (String tooltipProp);
}
