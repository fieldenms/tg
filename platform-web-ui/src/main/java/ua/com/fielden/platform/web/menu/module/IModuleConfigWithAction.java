package ua.com.fielden.platform.web.menu.module;

import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;

public interface IModuleConfigWithAction extends IModuleConfig0 {

    IModuleConfigWithAction withAction(final EntityActionConfig actionConfig);
}
