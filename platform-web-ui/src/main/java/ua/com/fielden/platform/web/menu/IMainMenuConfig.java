package ua.com.fielden.platform.web.menu;

import ua.com.fielden.platform.web.app.IWebApp;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;

/**
 * An API for configuring application's main menu
 *
 * @author TG Team
 *
 */
public interface IMainMenuConfig {

    /**
     * Adds new main menu item to the menu configuration object.
     *
     * @param title
     * @param icon
     * @param background
     * @return
     */
    IMainMenuConfig addMenuItem(String title, String icon, String background);

    /**
     * Adds "Return" menu item. "Return" action closes main menu.
     *
     * @param title
     * @param icon
     * @param background
     * @return
     */
    IMainMenuConfig addReturn(String title, String icon, String background);

    /**
     * Adds "Logout" menu item.
     *
     * @param title
     * @param icon
     * @param background
     * @return
     */
    IMainMenuConfig addLogout(String title, String icon, String background);

    /**
     * Adds "Login" menu item.
     *
     * @param title
     * @param icon
     * @param background
     * @return
     */
    IMainMenuConfig addLogin(String title, String icon, String background);

    /**
     * Set the main menu layout for specified device.
     *
     * @param device
     * @param layout
     * @return
     */
    IMainMenuConfig setLayoutWhen(Device device, String layout);

    /**
     * Finish to configure the application's main menu.
     *
     * @return
     */
    IWebApp endMenuConfig();

}
