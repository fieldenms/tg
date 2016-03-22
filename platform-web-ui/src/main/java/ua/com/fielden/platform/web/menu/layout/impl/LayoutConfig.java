package ua.com.fielden.platform.web.menu.layout.impl;

import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.interfaces.ILayout.Orientation;
import ua.com.fielden.platform.web.layout.TileLayout;
import ua.com.fielden.platform.web.menu.layout.IMenuLayoutConfig0;
import ua.com.fielden.platform.web.menu.layout.IMenuLayoutConfig1;
import ua.com.fielden.platform.web.menu.layout.IMenuLayoutConfig2;

public class LayoutConfig implements IMenuLayoutConfig0, IMenuLayoutConfig1, IMenuLayoutConfig2 {

    private final TileLayout tileLayout;
    private final IWebUiConfig webApp;

    public LayoutConfig(final TileLayout tileLayout, final IWebUiConfig webApp) {
        this.tileLayout = tileLayout;
        this.webApp = webApp;
    }

    @Override
    public IMenuLayoutConfig0 setLayoutFor(final Device device, final Orientation orientation, final String layout) {
        tileLayout.whenMedia(device, orientation).set(layout);
        return this;
    }

    @Override
    public IWebUiConfig done() {
        return webApp;
    }

    @Override
    public IMenuLayoutConfig2 minCellHeight(final int minCellHeight) {
        tileLayout.minCellHeight(minCellHeight);
        return this;
    }

    @Override
    public IMenuLayoutConfig1 minCellWidth(final int minCellWidth) {
        tileLayout.minCellWidth(minCellWidth);
        return this;
    }

}
