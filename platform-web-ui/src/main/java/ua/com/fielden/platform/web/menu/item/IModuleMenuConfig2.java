package ua.com.fielden.platform.web.menu.item;

import ua.com.fielden.platform.web.menu.IModuleMenuConfigWithDone;

public interface IModuleMenuConfig2 {

    IModuleMenuConfigWithDone done();

    IModuleMenuConfig3 addSubMenuItem(String title);
}
