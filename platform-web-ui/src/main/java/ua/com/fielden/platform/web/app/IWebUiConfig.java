package ua.com.fielden.platform.web.app;

import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.web.app.config.IWebUiBuilder;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.menu.IMainMenuBuilder;
import ua.com.fielden.platform.web.view.master.EntityMaster;

/**
 * Represent a contract for Web UI configuring.
 *
 * @author TG Team
 *
 */
public interface IWebUiConfig {

    /**
     * Provides access to the global application configuration object.
     *
     * @return
     */
    IWebUiBuilder configApp();

    /**
     * Provides access to the application's main menu configuration object.
     *
     * @return
     */
    IMainMenuBuilder configMainMenu();

    /**
     * Generates the web application.
     *
     * @return
     */
    String genAppIndex();

    /**
     * Generates the main menu component.
     *
     * @return
     */
    String genMainWebUIComponent();

    /**
     * Generates the global configuration component.
     *
     * @return
     */
    String genWebUiPreferences();

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
    Map<Class<? extends MiWithConfigurationSupport<?>>, EntityCentre<?>> getCentres();

    /**
     * Implement this in order to provide custom configurations for entity centre, master and other views.
     */
    void initConfiguration();
}
