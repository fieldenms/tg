package ua.com.fielden.platform.web.menu.module.impl;

import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.menu.IMainMenuConfigWithLayout;
import ua.com.fielden.platform.web.menu.IModuleConfig;
import ua.com.fielden.platform.web.menu.IModuleMenuConfig;
import ua.com.fielden.platform.web.menu.impl.MainMenuConfig;
import ua.com.fielden.platform.web.menu.module.IModuleConfig0;
import ua.com.fielden.platform.web.menu.module.IModuleConfig1;
import ua.com.fielden.platform.web.menu.module.IModuleConfig2;
import ua.com.fielden.platform.web.menu.module.IModuleConfig3;
import ua.com.fielden.platform.web.menu.module.IModuleConfig4;
import ua.com.fielden.platform.web.menu.module.IModuleConfigDone;

public class ModuleConfig implements IModuleConfig, IModuleConfig0, IModuleConfig1, IModuleConfig2, IModuleConfig3, IModuleConfig4, IModuleConfigDone {

    private final WebMenuModule module;
    private final MainMenuConfig menuConfig;

    public ModuleConfig(final MainMenuConfig menuConfig, final WebMenuModule module) {
        this.module = module;
        this.menuConfig = menuConfig;
    }

    @Override
    public IModuleConfig0 description(final String description) {
        module.description(description);
        return this;
    }

    @Override
    public IModuleConfig1 icon(final String icon) {
        module.icon(icon);
        return this;
    }

    @Override
    public IModuleConfig2 detailIcon(final String icon) {
        module.detailIcon(icon);
        return this;
    }

    @Override
    public IModuleConfig3 bgColor(final String htmlColor) {
        module.bgColor(htmlColor);
        return this;
    }

    @Override
    public IModuleConfig4 captionBgColor(final String htmlColor) {
        module.captionBgColor(htmlColor);
        return this;
    }

    @Override
    public IModuleConfigDone centre(final EntityCentre<?> centre) {
        // TODO Auto-generated method stub
        return this;
    }

    @Override
    public IModuleConfigDone view(final IRenderable view) {
        //TODO implement this
        return this;
    }

    @Override
    public IModuleMenuConfig menu() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IMainMenuConfigWithLayout done() {
        return menuConfig;
    }

}
