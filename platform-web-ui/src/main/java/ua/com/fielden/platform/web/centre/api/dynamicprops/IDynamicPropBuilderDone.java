package ua.com.fielden.platform.web.centre.api.dynamicprops;

import ua.com.fielden.platform.web.centre.api.IDynamicPropConfig;

/**
 * A contract that finishes the dynamic property configuration and returns its configuration for further use.
 *
 * @author TG Team
 *
 */
public interface IDynamicPropBuilderDone extends IDynamicPropBuilderAddProp{

    IDynamicPropConfig done();

}
