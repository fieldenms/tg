package ua.com.fielden.platform.web.menu.impl;

import ua.com.fielden.platform.web.app.IWebApp;
import ua.com.fielden.platform.web.interfaces.IExecutable;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.interfaces.ILayout.Orientation;
import ua.com.fielden.platform.web.layout.TileLayout;
import ua.com.fielden.platform.web.menu.IMainMenuConfigWithLayout;
import ua.com.fielden.platform.web.menu.IModuleConfig;
import ua.com.fielden.platform.web.menu.layout.IMenuLayoutConfig0;
import ua.com.fielden.platform.web.menu.layout.impl.LayoutConfig;
import ua.com.fielden.platform.web.menu.module.impl.ModuleConfig;
import ua.com.fielden.platform.web.minijs.JsCode;

public class MainMenuConfig implements IMainMenuConfigWithLayout, IExecutable {

    private final WebMainMenu mainMenu = new WebMainMenu();
    private final TileLayout tileLayout = new TileLayout();
    private final IWebApp webApp;

    public MainMenuConfig(final IWebApp webApp) {
        this.webApp = webApp;
    }

    @Override
    public IModuleConfig addModule(final String title) {
        final IModuleConfig moduleConfig = new ModuleConfig(this, mainMenu.addModule(title));
        return moduleConfig;
    }

    @Override
    public IMenuLayoutConfig0 setLayoutFor(final Device device, final Orientation orientation, final String layout) {
        final LayoutConfig layoutConfig = new LayoutConfig(tileLayout, webApp);
        layoutConfig.setLayoutFor(device, orientation, layout);
        return layoutConfig;
    }

    @Override
    public JsCode code() {
        final String desktopLayout = this.tileLayout.getLayout(Device.DESKTOP, null);
        final String tabletLayout = this.tileLayout.getLayout(Device.TABLET, null);
        final String mobileLayout = this.tileLayout.getLayout(Device.MOBILE, null);
        final StringBuilder menuConfig = new StringBuilder();
        if (desktopLayout != null) {
            menuConfig.append("whenDesktop: " + desktopLayout + ", ");
        }
        if (tabletLayout != null) {
            menuConfig.append("whenTablet: " + tabletLayout + ", ");
        }
        if (mobileLayout != null) {
            menuConfig.append("whenMobile: " + mobileLayout + ", ");
        }
        menuConfig.append("minCellWidth: \"" + tileLayout.getMinCellWidth() + "px\", ");
        menuConfig.append("minCellHeight: \"" + tileLayout.getMinCellHeight() + "px\", ");
        menuConfig.append("items: " + mainMenu.code().toString());
        return new JsCode("{" + menuConfig.toString() + "}");
    }
}
