package ua.com.fielden.platform.web.app;

import java.util.List;
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
     * Should return a domain name of a server like <code>tgdev.com</code> where the application is to be deployed.
     *
     * @return
     */
    String getDomainName();

    /**
     * Should return a path that follows the domain name where the application is to be bound to. For example, <code>/</code> for an application that is directly bound to the
     * domain name. Or, <code>/trident-fleet</code> is the application is bound to <code>https://www.fielden.com.au/trident-fleet</code>.
     *
     * @return
     */
    String getPath();

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

    /**
     * The paths for any kind of file resources those are needed for browser client. These are mapped to the '/resources/' router path. Also these resource paths might be augmented
     * with other custom paths. When client asks for a resource then this application will search for that resource in these paths starting from the custom ones.
     */
    List<String> resourcePaths();
}
