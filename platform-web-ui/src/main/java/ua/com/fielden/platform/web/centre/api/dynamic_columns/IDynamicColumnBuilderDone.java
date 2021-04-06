package ua.com.fielden.platform.web.centre.api.dynamic_columns;

import ua.com.fielden.platform.web.centre.api.IDynamicColumnConfig;

/**
 * A contract that completes configuration of dynamic columns and returns it for further use.
 *
 * @author TG Team
 *
 */
public interface IDynamicColumnBuilderDone extends IDynamicColumnBuilderAddProp {

    IDynamicColumnConfig done();

}
