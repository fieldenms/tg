package ua.com.fielden.platform.web.app;

import ua.com.fielden.platform.basic.config.Workflows;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.menu.IMenuRetriever;
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

import java.util.*;
import java.util.stream.Stream;

/// A contract for configuring the Web UI.
///
public interface IWebUiConfig extends IMenuRetriever {

    /// Returns the port on which the application server listens for incoming requests.
    ///
    int getPort();

    /// Returns the domain name of the server (for example, `tgdev.com`) where the application is deployed.
    ///
    String getDomainName();

    /// Returns the context path under the domain where the application is bound.
    /// For example, `/` for an application bound directly to the domain,
    /// or `/trident-fleet` if the application is bound to `https://www.fielden.com.au/trident-fleet`.
    ///
    String getPath();

    /// Provides access to the global Web UI application configuration builder.
    ///
    IWebUiBuilder configApp();

    /// Provides access to the desktop application's main menu configuration builder.
    ///
    IMainMenuBuilder configDesktopMainMenu();

    /// Provides access to the mobile application's main menu configuration builder.
    ///
    IMainMenuBuilder configMobileMainMenu();

    /// Generates the main HTML entry file of the Web application.
    ///
    String genAppIndex();

    /// Generates the main menu component for the desktop Web application.
    ///
    String genMainWebUIComponent();

    /// Generates the global Web UI configuration/preferences component.
    ///
    String genWebUiPreferences();

    /// Returns the instance of [IEventSourceEmitterRegister] created for this web application to manage registered clients.
    ///
    IEventSourceEmitterRegister getEventSourceEmitterRegister();

    /// Creates and registers an instance of `eventSourceClass` if it has not been created before.
    ///
    IWebUiConfig createAndRegisterEventSource(Class<? extends IEventSource> eventSourceClass);

    /// Returns the map of entity masters configured for this web application.
    ///
    Map<Class<? extends AbstractEntity<?>>, EntityMaster<? extends AbstractEntity<?>>>
    getMasters();

    /// Returns the map of entity centres configured for this web application.
    ///
    Map<Class<? extends MiWithConfigurationSupport<?>>, EntityCentre<?>>
    getCentres();

    /// Creates a stream of all action configurations defined in this Web UI configuration.
    ///
    Stream<EntityActionConfig> streamActionConfigs();

    /// Searches this Web UI configuration for an action with the specified identifier.
    ///
    Optional<EntityActionConfig> findAction(CharSequence actionIdentifier);

    /// Returns all registered “extra” actions that are not exposed in the UI but exist for server-side use only.
    ///
    Collection<EntityActionConfig> getExtraActions();

    /// Returns the map of custom views registered for this web application, keyed by view identifier.
    ///
    Map<String, AbstractCustomView> getCustomViews();

    /// Implement this method to provide custom configuration for entity centres, masters,
    /// and other Web UI views.
    ///
    void initConfiguration();

    /// Iterates through all registered [EntityCentre]s and creates default configurations for each of them.
    ///
    void createDefaultConfigurationsForAllCentres();

    /// Returns the map of embedded entity centres and their containing masters for this web application.
    ///
    Map<Class<? extends MiWithConfigurationSupport<?>>, T2<EntityCentre<?>, EntityMaster<? extends AbstractEntity<?>>>>
    getEmbeddedCentres();

    /// Loads all standalone and embedded default centres for the given `entityType`,
    /// including their generated types and criteria types.
    ///
    void loadCentreGeneratedTypesAndCriteriaTypes(final Class<?> entityType);

    /// Determines whether the centre represented by `miType` is embedded.
    ///
    default boolean isEmbeddedCentre(final Class<? extends MiWithConfigurationSupport<?>> miType) {
        return getEmbeddedCentres().containsKey(miType);
    }

    /// Determines whether the centre represented by `miType` is embedded and does not allow customisation.
    ///
    default boolean isEmbeddedCentreAndNotAllowCustomised(final Class<? extends MiWithConfigurationSupport<?>> miType) {
        return isEmbeddedCentre(miType) && getEmbeddedCentres().get(miType)._1.isRunAutomaticallyAndNotAllowCustomised();
    }

    /// Clears all centre, master, and menu configurations that were previously initialised.
    ///
    void clearConfiguration();

    /// Returns the list of base paths for static file resources needed by the Web client.
    /// These paths are mapped under the `/resources/` router path and may be augmented with additional custom paths.
    /// When a client requests a resource, the application searches these paths in order, starting from the custom ones.
    ///
    List<String> resourcePaths();

    /// Returns the current server- and client-side workflows configuration.
    ///
    Workflows workflow();

    /// Loads the checksum for a resource, if available, otherwise returns an empty [Optional].
    ///
    /// Checksums are available for selected static resources in deployment mode (e.g. `startup-resources-vulcanized.js`).
    /// The client-side Service Worker first requests the checksum to detect changes.
    /// If the checksum differs, the full resource is re-fetched and re-cached.
    /// Otherwise, the cached version is used.
    ///
    Optional<String> checksum(final String resourceURI);

    /// Returns the set of actions used for centre configuration sharing.
    ///
    List<EntityActionConfig> centreConfigShareActions();

    /// Sets the minimum screen width at which the UI is treated as a desktop layout.
    ///
    IWebUiConfig setMinDesktopWidth(final int width);

    /// Sets the minimum screen width at which the UI is treated as a tablet layout.
    ///
    IWebUiConfig setMinTabletWidth(final int width);

    /// Sets the locale to use for client-side number formatting.
    ///
    IWebUiConfig setLocale(final String locale);

    /// Sets the time format pattern to be used on the client side of the application.
    ///
    IWebUiConfig setTimeFormat(final String timeFormat);

    /// Sets the time format pattern with milliseconds to be used on the client side of the application.
    ///
    IWebUiConfig setTimeWithMillisFormat(final String timeWithMillisFormat);

    /// Sets the date format pattern to be used on the client side of the application.
    ///
    IWebUiConfig setDateFormat(final String dateFormat);

    /// Sets the colour of the web application’s main top panel.
    ///
    IWebUiConfig setMainPanelColor(final String panelColor);

    /// Sets the watermark text displayed on the application’s main top panel.
    ///
    IWebUiConfig setWatermark(final String watermark);

    /// Sets the style to use for rendering the watermark text.
    ///
    IWebUiConfig setWatermarkStyle(final String watermarkStyle);

    /// Indicates whether the server and client applications operate in independent time zones.
    ///
    /// The only difference is handling of `now`, which is calculated in the real user time zone
    /// and then converted to the server time zone.
    ///
    boolean independentTimeZone();

    /// Returns the minimum screen width at which the device is treated as a desktop.
    ///
    int minDesktopWidth();

    /// Returns the minimum screen width at which the device is treated as a tablet.
    ///
    int minTabletWidth();

    /// Returns the locale to use for client-side number formatting.
    ///
    String locale();

    /// Returns the date format pattern to be used on the client side of the application.
    ///
    String dateFormat();

    /// Returns the time format pattern to be used on the client side of the application.
    ///
    String timeFormat();

    /// Returns the time-with-milliseconds format pattern to be used on the client side of the application.
    ///
    String timeWithMillisFormat();

    /// Returns the value that determines which options should be available in master actions.
    ///
    String masterActionOptions();

    /// Returns the application title displayed in the browser tab.
    ///
    String title();

    /// Returns the URI of the “idea” action.
    ///
    String ideaUri();

    /// Returns the colour of the application’s main top panel.
    ///
    String mainPanelColor();

    /// Returns the watermark text displayed on the application’s main top panel.
    ///
    String watermark();

    /// Returns the style of the watermark text.
    ///
    String watermarkStyle();

    /// Returns the set of site URLs that users may visit without confirmation.
    ///
    Set<String> siteAllowList();

    /// Returns the number of days an allowed site remains trusted before requiring re-confirmation.
    ///
    int daysUntilSitePermissionExpires();

}
