package ua.com.fielden.platform.web.centre.api.dynamic_columns;

import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;

/**
 * A contract to specify the name of a property that holds a tooltip for a column value
 *
 * @author TG Team
 *
 */
public interface IDynamicColumnBuilderWithTooltipProp extends IDynamicColumnBuilderAddProp, IDynamicColumnBuilderDone{

    IDynamicColumnBuilderAddProp withTooltipProp(final String tooltipProp);

    default IDynamicColumnBuilderAddProp withTooltipProp(final IConvertableToPath tooltipProp) {
        return withTooltipProp(tooltipProp.toPath());
    }

}