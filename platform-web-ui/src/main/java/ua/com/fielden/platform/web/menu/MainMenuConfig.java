package ua.com.fielden.platform.web.menu;

import static org.apache.commons.lang.StringUtils.deleteWhitespace;
import static org.apache.commons.lang.StringUtils.uncapitalize;
import static org.apache.commons.lang.WordUtils.capitalize;

import java.util.LinkedHashMap;
import java.util.Map;

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
    private final LinkedHashMap<String, MainMenuItemConfig> menuItems = new LinkedHashMap<>();
    private final FlexLayout flexLayout = new FlexLayout();

    private String returnMenuItem = "", loginMenuItem = "", logoutMenuItem = "";

    public MainMenuConfig(final IWebApp webApplication) {
        this.webApplication = webApplication;
    }

    @Override
    public IMainMenuItemConfig addMenuItem(final String title) {
        if (!menuItems.containsKey(title)) {
            final MainMenuItemConfig menuItemConfig = new MainMenuItemConfig(this, title);
            menuItems.put(title, menuItemConfig);
            return menuItemConfig;
        } else {
            throw new IllegalArgumentException("The menu item with " + title + " title already exists!");
        }
    }

    @Override
    public IMainMenuItemConfig addReturn(final String title) {
        final IMainMenuItemConfig menuItemConfig = addMenuItem(title);
        this.returnMenuItem = title;
        return menuItemConfig;
    }

    @Override
    public IMainMenuItemConfig addLogout(final String title) {
        final IMainMenuItemConfig menuItemConfig = addMenuItem(title);
        this.loginMenuItem = title;
        return menuItemConfig;
    }

    @Override
    public IMainMenuItemConfig addLogin(final String title) {
        final IMainMenuItemConfig menuItemConfig = addMenuItem(title);
        this.logoutMenuItem = title;
        return menuItemConfig;
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
        for (final Map.Entry<String, MainMenuItemConfig> menuItem : menuItems.entrySet()) {
            flexElement.add(menuItem.getValue().render().attr("id", generateMenuItemId(menuItem.getKey())).attr("on-tap", "{{onMenuTap}}"));
        }
        return ResourceLoader.getText("ua/com/fielden/platform/web/menu/tg-main-menu.html").
                replaceAll("@returnId", "\"" + generateMenuItemId(returnMenuItem) + "\"").
                replaceAll("@loginId", "\"" + generateMenuItemId(loginMenuItem) + "\"").
                replaceAll("@logoutId", "\"" + generateMenuItemId(logoutMenuItem) + "\"").
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
}
