package ua.com.fielden.platform.web.app;

import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.basic.config.Workflows;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.web.app.config.IWebUiBuilder;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.custom_view.AbstractCustomView;
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
     * Should return a port that an application server is listening to for incoming requests.
     * 
     * @return
     */
    int getPort();
    
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
     * Provides access to the desktop application's main menu configuration object.
     *
     * @return
     */
    IMainMenuBuilder configDesktopMainMenu();

    /**
     * Provides access to the mobile application's main menu configuration object.
     *
     * @return
     */
    IMainMenuBuilder configMobileMainMenu();

    /**
     * Generates the main html file of desktop web application.
     *
     * @return
     */
    String genDesktopAppIndex();

    /**
     * Generates the main html file of mobile web application.
     *
     * @return
     */
    String genMobileAppIndex();

    /**
     * Generates the main menu component for mobile application.
     *
     * @return
     */
    String genMobileMainWebUIComponent();

    /**
     * Generates the main menu component for desktop application.
     *
     * @return
     */
    String genDesktopMainWebUIComponent();

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
     * Returns the map of custom views for this web application.
     *
     * @return
     */
    Map<String, AbstractCustomView> getCustomViews();

    /**
     * Implement this in order to provide custom configurations for entity centre, master and other views.
     */
    void initConfiguration();

    /**
     * The paths for any kind of file resources those are needed for browser client. These are mapped to the '/resources/' router path. Also these resource paths might be augmented
     * with other custom paths. When client asks for a resource then this application will search for that resource in these paths starting from the custom ones.
     */
    List<String> resourcePaths();
    
    /**
     * The current {@link Workflows} of the server and client applications.
     * 
     * @return
     */
    Workflows workflow();
}
