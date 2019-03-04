package ua.com.fielden.platform.web.menu.item.impl;

import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.custom_view.AbstractCustomView;
import ua.com.fielden.platform.web.menu.IModuleMenuConfigWithDone;
import ua.com.fielden.platform.web.menu.item.IModuleMenuConfig0;
import ua.com.fielden.platform.web.menu.item.IModuleMenuConfig1;
import ua.com.fielden.platform.web.menu.item.IModuleMenuConfig1WithIcon;
import ua.com.fielden.platform.web.menu.item.IModuleMenuConfig2;
import ua.com.fielden.platform.web.menu.item.IModuleMenuConfig2WithDone;
import ua.com.fielden.platform.web.menu.item.IModuleMenuConfig3;
import ua.com.fielden.platform.web.menu.item.IModuleMenuConfigWithView;
import ua.com.fielden.platform.web.menu.module.impl.ModuleMenuConfig;
import ua.com.fielden.platform.web.menu.module.impl.WebMenuItem;
import ua.com.fielden.platform.web.menu.module.impl.WebView;
import ua.com.fielden.platform.web.view.master.EntityMaster;

public class MenuItemConfig implements IModuleMenuConfig0, IModuleMenuConfig1WithIcon, IModuleMenuConfigWithView, IModuleMenuConfig2, IModuleMenuConfig2WithDone {

    private final WebMenuItem menuItem;
    private final ModuleMenuConfig moduleMenuConfig;

    public MenuItemConfig(final WebMenuItem menuItem, final ModuleMenuConfig moduleMenuConfig) {
        this.menuItem = menuItem;
        this.moduleMenuConfig = moduleMenuConfig;
    }

    @Override
    public IModuleMenuConfig1WithIcon description(final String desc) {
        menuItem.description(desc);
        return this;
    }

    @Override
    public IModuleMenuConfigWithView centre(final EntityCentre<?> centre) {
        menuItem.view(new WebView(centre));
        return this;
    }

    @Override
    public IModuleMenuConfigWithView master(final EntityMaster<?> entityMaster) {
        menuItem.view(new WebView(entityMaster));
        return this;
    }

    @Override
    public IModuleMenuConfigWithView view(final AbstractCustomView view) {
        menuItem.view(new WebView(view));
        return this;
    }

    @Override
    public IModuleMenuConfigWithDone done() {
        return moduleMenuConfig;
    }

    @Override
    public IModuleMenuConfig3 addMenuItem(final String title) {
        return new SubMenuItemConfig(menuItem.addMenuItem(title), this);
    }

    @Override
    public IModuleMenuConfig1 icon(final String icon) {
        menuItem.icon(icon);
        return this;
    }
}
