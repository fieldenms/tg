package ua.com.fielden.platform.web.app;

import ua.com.fielden.platform.basic.config.Workflows;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.menu.IMenuRetriever;
import ua.com.fielden.platform.menu.IWebAppConfigProvider;
import ua.com.fielden.platform.menu.IWebAppConfigSetter;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.web.app.config.IWebUiBuilder;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.custom_view.AbstractCustomView;
import ua.com.fielden.platform.web.menu.IMainMenuBuilder;
import ua.com.fielden.platform.web.sse.IEventSource;
import ua.com.fielden.platform.web.sse.IEventSourceEmitterRegister;
import ua.com.fielden.platform.web.view.master.EntityMaster;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/// Represent a contract for Web UI configuring.
///
public interface IWebUiConfig extends IWebAppConfigProvider, IWebAppConfigSetter, IMenuRetriever {

    /// Should return a port that an application server is listening to for incoming requests.
    ///
    int getPort();

    /// Should return a domain name of a server like `tgdev.com` where the application is to be deployed.
    ///
    String getDomainName();

    /// Should return a path that follows the domain name where the application is to be bound to. For example, `/` for an application that is directly bound to the
    /// domain name. Or, `/trident-fleet` is the application is bound to `https://www.fielden.com.au/trident-fleet`.
    ///
    String getPath();

    /// Provides access to the global application configuration object.
    ///
    IWebUiBuilder configApp();

    /// Provides access to the desktop application's main menu configuration object.
    ///
    IMainMenuBuilder configDesktopMainMenu();

    /// Provides access to the mobile application's main menu configuration object.
    ///
    IMainMenuBuilder configMobileMainMenu();

    /// Generates the main html file of web application.
    ///
    String genAppIndex();

    /// Generates the main menu component for desktop application.
    ///
    String genMainWebUIComponent();

    /// Generates the global configuration component.
    ///
    String genWebUiPreferences();

    /// Returns the instance of [IEventSourceEmitterRegister] that will is created for this web application to manage registered clients.
    ///
    IEventSourceEmitterRegister getEventSourceEmitterRegister();

    /// Creates and registers an instance of `eventSrouceClass`, if it was not created before.
    ///
    IWebUiConfig createAndRegisterEventSource(Class<? extends IEventSource> eventSourceClass);

    /// Returns the map of entity masters for this web application.
    ///
    Map<Class<? extends AbstractEntity<?>>, EntityMaster<? extends AbstractEntity<?>>> getMasters();

    /// Returns the map of entity centres for this web application.
    ///
    Map<Class<? extends MiWithConfigurationSupport<?>>, EntityCentre<?>> getCentres();

    /// Creates a stream of all action configurations in the whole Web UI configuration.
    ///
    Stream<EntityActionConfig> streamActionConfigs();

    /// Searches the whole Web UI configuration for an action with the specified identifier.
    ///
    Optional<EntityActionConfig> findAction(CharSequence actionIdentifier);

    /// Returns all registered "extra" actions, which are not exposed in the UI, but exist for other server-side purposes.
    ///
    Collection<EntityActionConfig> getExtraActions();

    /// Returns the map of custom views for this web application.
    ///
    Map<String, AbstractCustomView> getCustomViews();

    /// Implement this in order to provide custom configurations for entity centre, master and other views.
    ///
    void initConfiguration();

    /// Iterates through all registered [EntityCentre]s and creates default configurations for each one.
    ///
    void createDefaultConfigurationsForAllCentres();

    /// Returns the map of embedded entity centres (and masters containing them) for this web application.
    ///
    Map<Class<? extends MiWithConfigurationSupport<?>>, T2<EntityCentre<?>, EntityMaster<? extends AbstractEntity<?>>>> getEmbeddedCentres();

    /// Loads all standalone / embedded default centres for concrete 'entityType' (with their generated types and criteria types).
    ///
    void loadCentreGeneratedTypesAndCriteriaTypes(final Class<?> entityType);

    /// Determines whether the centre, represented by `miType`, is embedded.
    ///
    default boolean isEmbeddedCentre(final Class<? extends MiWithConfigurationSupport<?>> miType) {
        return getEmbeddedCentres().containsKey(miType);
    }

    /// Determines whether the centre, represented by `miType`, is embedded and does not allow customisation.
    ///
    default boolean isEmbeddedCentreAndNotAllowCustomised(final Class<? extends MiWithConfigurationSupport<?>> miType) {
        return isEmbeddedCentre(miType) && getEmbeddedCentres().get(miType)._1.isRunAutomaticallyAndNotAllowCustomised();
    }

    /// Clears all centre, master and menu configurations that were initialised before.
    ///
    void clearConfiguration();

    /// The paths for any kind of file resources those are needed for browser client. These are mapped to the `/resources/` router path. Also, these resource paths might be augmented
    /// with other custom paths. When client asks for a resource then this application will search for that resource in these paths starting from the custom ones.
    ///
    List<String> resourcePaths();

    /// The current [Workflows] of the server and client applications.
    ///
    Workflows workflow();

    /// Loads checksum for resource if available. Otherwise, returns empty [Optional].
    ///
    /// Checksums are available for static resources in deployment mode. `startup-resources-vulcanized.js` file is primary in this category.
    /// Client-side Service Worker script intercepts requests to get checksum first to compare whether resource has changed.
    /// If that is true then full resource will be re-downloaded and re-cached on the client side.
    /// Otherwise, the cached resource will be used straight away.
    ///
    Optional<String> checksum(final String resourceURI);

    /// A set of domain-specific actions for centre configurations sharing.
    ///
    List<EntityActionConfig> centreConfigShareActions();

}
