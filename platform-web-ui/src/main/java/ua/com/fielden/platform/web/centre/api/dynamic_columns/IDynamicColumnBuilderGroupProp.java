package ua.com.fielden.platform.web.centre.api.dynamic_columns;

import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;

/**
 * A contract that specifies the name of a property that holds the group name.
 *
 * @author TG Team
 *
 */
public interface IDynamicColumnBuilderGroupProp {

    IDynamicColumnBuilderDisplayProp withGroupProp(final String groupProp);

    default IDynamicColumnBuilderDisplayProp withGroupProp(final IConvertableToPath groupProp) {
        return withGroupProp(groupProp.toPath());
    }

}