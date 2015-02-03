package ua.com.fielden.platform.web.menu;

import java.util.LinkedHashMap;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.web.app.IWebApp;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.layout.FlexLayout;

public class MainMenuConfig implements IMainMenuConfig {

    /**
     * The {@link IWebApp} instance for which this main menu configuration object was created.
     */
    private final IWebApp webApplication;
    private final LinkedHashMap<String, MainMenuItem> menuItems = new LinkedHashMap<>();
    private final FlexLayout flexLayout = new FlexLayout();

    private String returnMenuItem, loginMenuItem, logoutMenuItem;

    public MainMenuConfig(final IWebApp webApplication) {
        this.webApplication = webApplication;
    }

    @Override
    public IMainMenuConfig addMenuItem(final String title, final String icon, final String background) {
        if (!menuItems.containsKey(title)) {
            menuItems.put(title, new MainMenuItem(title, icon, background));
        } else {
            throw new IllegalArgumentException("The menu item with " + title + " title already exists!");
        }
        return this;
    }

    @Override
    public IMainMenuConfig addReturn(final String title, final String icon, final String background) {
        addMenuItem(title, icon, background);
        this.returnMenuItem = title;
        return this;
    }

    @Override
    public IMainMenuConfig addLogout(final String title, final String icon, final String background) {
        addMenuItem(title, icon, background);
        this.loginMenuItem = title;
        return this;
    }

    @Override
    public IMainMenuConfig addLogin(final String title, final String icon, final String background) {
        addMenuItem(title, icon, background);
        this.logoutMenuItem = title;
        return this;
    }

    @Override
    public IMainMenuConfig setLayoutWhen(final Device device, final String layout) {
        flexLayout.whenMedia(device).set(layout);
        return this;
    }

    @Override
    public IWebApp endMenuConfig() {
        return webApplication;
    }

    public String generateMainMenu() {
        final DomElement flexElement = flexLayout.render();
        for(final MainMenuItem menuItem: menuItems.values()) {
            flexElement.add(createMenuItemElement(menuItem));
        }
        return "<div class="tile">
                <paper-shadow z="1" class="category return" id="return" on-tap="{{onMenuTap}}" layout vertical end-justified center fit>
                    <core-icon class="svg-normal" src="/resources/images/Return.svg"></core-icon>
                    <pre>Return</pre>
                </paper-shadow>
            </div>"
    }

    private DomElement createMenuItemElement(final MainMenuItem menuItem) {
        return null;
    }
}
