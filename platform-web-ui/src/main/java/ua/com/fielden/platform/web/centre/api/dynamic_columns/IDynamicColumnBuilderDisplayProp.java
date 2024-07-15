package ua.com.fielden.platform.web.centre.api.dynamic_columns;

import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;

/**
 * A contract that specifies the name of a property that holds the value to be displayed.
 *
 * @author TG Team
 *
 */
public interface IDynamicColumnBuilderDisplayProp {

    IDynamicColumnBuilderWithTooltipProp withDisplayProp(final String displayProp);

    default IDynamicColumnBuilderWithTooltipProp withDisplayProp(final IConvertableToPath displayProp) {
        return withDisplayProp(displayProp.toPath());
    }

}