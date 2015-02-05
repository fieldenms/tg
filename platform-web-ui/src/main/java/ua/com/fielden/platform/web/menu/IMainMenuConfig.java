package ua.com.fielden.platform.web.menu;

import ua.com.fielden.platform.web.app.IWebApp;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.interfaces.ILayout.Orientation;

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
    IMainMenuConfig addMenuItem(final String title, final String icon, final String background);

    /**
     * Adds "Return" menu item. "Return" action closes main menu.
     *
     * @param title
     * @param icon
     * @param background
     * @return
     */
    IMainMenuConfig addReturn(final String title, final String icon, final String background);

    /**
     * Adds "Logout" menu item.
     *
     * @param title
     * @param icon
     * @param background
     * @return
     */
    IMainMenuConfig addLogout(final String title, final String icon, final String background);

    /**
     * Adds "Login" menu item.
     *
     * @param title
     * @param icon
     * @param background
     * @return
     */
    IMainMenuConfig addLogin(final String title, final String icon, final String background);

    /**
     * Set the main menu layout for specified device.
     *
     * @param device
     * @param layout
     * @return
     */
    IMainMenuConfig setLayoutFor(final Device device, final Orientation orientation, final String layout);

    /**
     * Finish to configure the application's main menu.
     *
     * @return
     */
    IWebApp done();

}
