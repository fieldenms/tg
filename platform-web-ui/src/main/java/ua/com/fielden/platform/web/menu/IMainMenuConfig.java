package ua.com.fielden.platform.web.menu;

import ua.com.fielden.platform.web.app.IWebApp;

/**
 * An API for configuring application's main menu
 *
 * @author TG Team
 *
 */
public interface IMainMenuConfig extends IMenuLayoutConfig<IMainMenuConfig> {

    //    /**
    //     * Adds new master main menu item with specified title to the menu configuration object.
    //     *
    //     * @param title
    //     * @param entityType
    //     * @return
    //     */
    //    IMainMenuItemConfig addMasterMenuItem(String title, Class<? extends AbstractEntity<?>> entityType);
    //
    //    /**
    //     * Adds new centre main menu item with specified title to the menu configuration object.
    //     *
    //     * @param title
    //     * @param menuItemType
    //     * @return
    //     */
    //    IMainMenuItemConfig addCentreMenuItem(String title, Class<? extends MiWithConfigurationSupport<?>> menuItemType);
    //
    //    /**
    //     * Adds "Return" menu item. "Return" action closes main menu.
    //     *
    //     * @param title
    //     * @return
    //     */
    //    IMainMenuItemConfig addReturn(String title);
    //
    //    /**
    //     * Adds "Logout" menu item.
    //     *
    //     * @param title
    //     * @return
    //     */
    //    IMainMenuItemConfig addLogout(String title);
    //
    //    /**
    //     * Adds "Login" menu item.
    //     *
    //     * @param title
    //     * @return
    //     */
    //    IMainMenuItemConfig addLogin(String title);

    IModuleConfig addModule(String title);

    /**
     * Finish to configure the application's main menu.
     *
     * @return
     */
    IWebApp done();

}
