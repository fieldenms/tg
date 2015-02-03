package ua.com.fielden.platform.web.app;

import ua.com.fielden.platform.web.app.config.IWebAppConfig;
import ua.com.fielden.platform.web.menu.IMainMenuConfig;

/**
 * API for web application configuring.
 *
 * @author TG Team
 *
 */
public interface IWebApp {

    /**
     * Provides access to the global application configuration object.
     *
     * @return
     */
    IWebAppConfig configApp();

    /**
     * Provides access to the application's main menu configuration object.
     *
     * @return
     */
    IMainMenuConfig configMainMenu();
}
