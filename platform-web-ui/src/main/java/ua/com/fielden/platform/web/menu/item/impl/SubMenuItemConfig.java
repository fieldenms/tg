package ua.com.fielden.platform.web.menu.item.impl;

import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.custom_view.AbstractCustomView;
import ua.com.fielden.platform.web.menu.item.IModuleMenuConfig2;
import ua.com.fielden.platform.web.menu.item.IModuleMenuConfig3;
import ua.com.fielden.platform.web.menu.item.IModuleMenuConfig4;
import ua.com.fielden.platform.web.menu.item.IModuleMenuConfig5;
import ua.com.fielden.platform.web.menu.module.impl.WebSubMenuItem;
import ua.com.fielden.platform.web.menu.module.impl.WebView;

public class SubMenuItemConfig implements IModuleMenuConfig3, IModuleMenuConfig4, IModuleMenuConfig5 {

    private final WebSubMenuItem subMenuItem;
    private final MenuItemConfig menuItemConfig;

    public SubMenuItemConfig(final WebSubMenuItem subMenuItem, final MenuItemConfig menuItemConfig) {
        this.subMenuItem = subMenuItem;
        this.menuItemConfig = menuItemConfig;
    }

    @Override
    public IModuleMenuConfig4 description(final String desc) {
        subMenuItem.description(desc);
        return this;
    }

    @Override
    public IModuleMenuConfig5 centre(final EntityCentre<?> centre) {
        subMenuItem.view(new WebView(centre));
        return this;
    }

    @Override
    public IModuleMenuConfig5 view(final AbstractCustomView view) {
        subMenuItem.view(new WebView(view));
        return this;
    }

    @Override
    public IModuleMenuConfig2 done() {
        return menuItemConfig;
    }

}
