package ua.com.fielden.platform.web.menu;


/**
 * An API entry point for building application's main menu.
 *
 * @author TG Team
 *
 */
public interface IMainMenuBuilder {

    /**
     * Adds new module to main menu, meaning that each main menu item is associated with a logical application module.
     *
     * @param title
     * @return
     */
    IModuleConfig addModule(String title);
}
