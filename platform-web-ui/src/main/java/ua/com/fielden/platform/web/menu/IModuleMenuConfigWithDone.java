package ua.com.fielden.platform.web.menu;

import ua.com.fielden.platform.web.menu.module.IModuleConfigDone;

public interface IModuleMenuConfigWithDone extends IModuleMenuConfig {

    IModuleConfigDone done();
}
