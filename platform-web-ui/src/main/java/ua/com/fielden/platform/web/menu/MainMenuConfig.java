package ua.com.fielden.platform.web.menu;

import static org.apache.commons.lang.StringUtils.deleteWhitespace;
import static org.apache.commons.lang.StringUtils.uncapitalize;
import static org.apache.commons.lang.WordUtils.capitalize;

import java.util.LinkedHashMap;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.utils.ResourceLoader;
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

    private String returnMenuItem = "", loginMenuItem = "", logoutMenuItem = "";

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
        for (final MainMenuItem menuItem : menuItems.values()) {
            flexElement.add(menuItem.render().attr("id", generateMenuItemId(menuItem.title)).attr("on-tap", "{{onMenuTap}}"));
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
