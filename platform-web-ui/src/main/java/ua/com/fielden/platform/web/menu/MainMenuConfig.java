package ua.com.fielden.platform.web.menu;

import static org.apache.commons.lang.StringUtils.deleteWhitespace;
import static org.apache.commons.lang.StringUtils.uncapitalize;
import static org.apache.commons.lang.WordUtils.capitalize;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.app.IWebApp;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.interfaces.ILayout.Orientation;
import ua.com.fielden.platform.web.layout.FlexLayout;

public class MainMenuConfig implements IMainMenuConfig {

    /**
     * The {@link IWebApp} instance for which this main menu configuration object was created.
     */
    private final IWebApp webApplication;
    private final List<MainMenuItemConfig> menuItems = new ArrayList<>();
    private final FlexLayout flexLayout = new FlexLayout();

    private MainMenuItemConfig returnMenuItem, loginMenuItem, logoutMenuItem;

    public MainMenuConfig(final IWebApp webApplication) {
        this.webApplication = webApplication;
    }

    @Override
    public IMainMenuItemConfig addMenuItem() {
        return addMainMenuItem();
    }

    @Override
    public IMainMenuItemConfig addReturn() {
        returnMenuItem = addMainMenuItem();
        return returnMenuItem;
    }

    @Override
    public IMainMenuItemConfig addLogout() {
        logoutMenuItem = addMainMenuItem();
        return logoutMenuItem;
    }

    @Override
    public IMainMenuItemConfig addLogin() {
        loginMenuItem = addMainMenuItem();
        return loginMenuItem;
    }

    @Override
    public IMainMenuConfig setLayoutFor(final Device device, final Orientation orientation, final String layout) {
        flexLayout.whenMedia(device).set(layout);
        return this;
    }

    @Override
    public IWebApp done() {
        return webApplication;
    }

    public String generateMainMenu() {
        final DomElement flexElement = flexLayout.render();
        for (final MainMenuItemConfig menuItem : menuItems) {
            flexElement.add(menuItem.render().attr("id", generateMenuItemId(menuItem.getTitle())).attr("on-tap", "{{onMenuTap}}"));
        }
        return ResourceLoader.getText("ua/com/fielden/platform/web/menu/tg-main-menu.html").
                replaceAll("@returnId", "\"" + generateMenuItemId(returnMenuItem.getTitle()) + "\"").
                replaceAll("@loginId", "\"" + generateMenuItemId(loginMenuItem.getTitle()) + "\"").
                replaceAll("@logoutId", "\"" + generateMenuItemId(logoutMenuItem.getTitle()) + "\"").
                replaceAll("@menu", flexElement.toString());
    }

    /**
     * Generates the id for menu item elements.
     *
     * @param title
     * @return
     */
    private String generateMenuItemId(final String title) {
        return uncapitalize(deleteWhitespace(capitalize(title)));
    }

    /**
     * Creates new main menu item and adds it to the list
     *
     * @return
     */
    private MainMenuItemConfig addMainMenuItem() {
        final MainMenuItemConfig menuItemConfig = new MainMenuItemConfig(this);
        menuItems.add(menuItemConfig);
        return menuItemConfig;
    }
}
