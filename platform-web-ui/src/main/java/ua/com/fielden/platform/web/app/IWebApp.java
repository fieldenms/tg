package ua.com.fielden.platform.web.app;

import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.web.app.config.IWebAppConfig;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.menu.IMainMenuConfig;
import ua.com.fielden.platform.web.view.master.EntityMaster;

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

    /**
     * Generates the web application.
     *
     * @return
     */
    String generateWebApp();

    /**
     * Generates the main menu component.
     *
     * @return
     */
    String generateMainMenu();

    /**
     * Generates the global configuration component.
     *
     * @return
     */
    String generateGlobalConfig();

    /**
     * Returns the map of entity masters for this web application.
     *
     * @return
     */
    Map<Class<? extends AbstractEntity<?>>, EntityMaster<? extends AbstractEntity<?>>> getMasters();

    /**
     * Returns the map of entity centres for this web application.
     *
     * @return
     */
    Map<Class<? extends MiWithConfigurationSupport<?>>, EntityCentre> getCentres();

    /**
     * Implement this in order to provide custom configurations for entity centre, master and other views.
     */
    void initConfiguration();
}
