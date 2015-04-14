package ua.com.fielden.platform.web.menu;


/**
 * An API for configuring application's main menu
 *
 * @author TG Team
 *
 */
public interface IMainMenuConfig {

    /**
     * Adds new module to main menu
     *
     * @param title
     * @return
     */
    IModuleConfig addModule(String title);
}
