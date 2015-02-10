package ua.com.fielden.platform.web.menu;

import ua.com.fielden.platform.web.app.IWebApp;

/**
 * An API for configuring application's main menu
 *
 * @author TG Team
 *
 */
public interface IMainMenuConfig extends IMenuLayoutConfig<IMainMenuConfig> {

    /**
     * Adds new main menu item to the menu configuration object.
     *
     * @return
     */
    IMainMenuItemConfig addMenuItem();

    /**
     * Adds "Return" menu item. "Return" action closes main menu.
     *
     * @return
     */
    IMainMenuItemConfig addReturn();

    /**
     * Adds "Logout" menu item.
     *
     * @return
     */
    IMainMenuItemConfig addLogout();

    /**
     * Adds "Login" menu item.
     *
     * @return
     */
    IMainMenuItemConfig addLogin();

    /**
     * Finish to configure the application's main menu.
     *
     * @return
     */
    IWebApp done();

}
