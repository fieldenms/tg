package ua.com.fielden.platform.web.menu.module.impl;

import ua.com.fielden.platform.web.menu.IModuleMenuConfig;
import ua.com.fielden.platform.web.menu.IModuleMenuConfigWithDone;
import ua.com.fielden.platform.web.menu.item.IModuleMenuConfig0;
import ua.com.fielden.platform.web.menu.item.impl.MenuItemConfig;
import ua.com.fielden.platform.web.menu.module.IModuleConfigDone;

public class ModuleMenuConfig implements IModuleMenuConfig, IModuleMenuConfigWithDone {

    private final WebMenu menu;
    private final ModuleConfig moduleConfig;

    public ModuleMenuConfig(final WebMenu menu, final ModuleConfig moduleConfig) {
        this.menu = menu;
        this.moduleConfig = moduleConfig;
    }

    @Override
    public IModuleConfigDone done() {
        return moduleConfig;
    }

    @Override
    public IModuleMenuConfig0 addMenuItem(final String title) {
        return new MenuItemConfig(menu.addMenuItem(title), this);
    }

}
