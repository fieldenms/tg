package ua.com.fielden.platform.web.menu.impl;

import ua.com.fielden.platform.menu.Menu;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.interfaces.ILayout.Orientation;
import ua.com.fielden.platform.web.layout.TileLayout;
import ua.com.fielden.platform.web.menu.IMainMenuBuilderWithLayout;
import ua.com.fielden.platform.web.menu.IModuleConfig;
import ua.com.fielden.platform.web.menu.layout.IMenuLayoutConfig0;
import ua.com.fielden.platform.web.menu.layout.impl.LayoutConfig;
import ua.com.fielden.platform.web.menu.module.impl.ModuleConfig;

/**
 * An implementation of {@link IMainMenuBuilderWithLayout} contract, which serves both as the main menu builder and the representation of the final main menu configuration.
 *
 * @author TG Team
 *
 */
public class MainMenuBuilder implements IMainMenuBuilderWithLayout {

    private final WebMainMenu mainMenu = new WebMainMenu();
    private final TileLayout tileLayout = new TileLayout();
    private final IWebUiConfig webApp;

    public MainMenuBuilder(final IWebUiConfig webApp) {
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

    public EntityActionConfig getActionConfig(final int actionNumber, final FunctionalActionKind actionKind) {
        return mainMenu.getActionConfig(actionNumber, actionKind);
    }

    public Menu getMenu() {
        return new Menu().
                setMenu(mainMenu.getModules()).
                setMinCellWidth(tileLayout.getMinCellWidth() + "px").
                setMinCellHeight(tileLayout.getMinCellHeight() + "px").
                setWhenDesktop(tileLayout.getLayout(Device.DESKTOP, null).get()).
                setWhenTablet(tileLayout.getLayout(Device.TABLET, null).get()).
                setWhenMobile(tileLayout.getLayout(Device.MOBILE, null).get());
    }
}
