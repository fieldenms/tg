package ua.com.fielden.platform.web.centre.api.dynamic_columns;

import ua.com.fielden.platform.web.centre.api.IDynamicColumnConfig;

/**
 * A contract that adds a new dynamic column with a specific group property value or finishes the dynamic column configuration.
 *
 * @author TG Team
 *
 */
public interface IDynamicColumnBuilderAddPropWithDone extends IDynamicColumnBuilderAddProp {

    IDynamicColumnConfig done();
}