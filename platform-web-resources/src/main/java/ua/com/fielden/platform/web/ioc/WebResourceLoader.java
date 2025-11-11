package ua.com.fielden.platform.web.ioc;

import com.google.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.basic.config.Workflows;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.api.impl.TgJackson;
import ua.com.fielden.platform.serialisation.jackson.EntityType;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.utils.StreamUtils;
import ua.com.fielden.platform.web.app.IWebResourceLoader;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionElement;
import ua.com.fielden.platform.web.custom_view.AbstractCustomView;
import ua.com.fielden.platform.web.ioc.exceptions.MissingCentreConfigurationException;
import ua.com.fielden.platform.web.ioc.exceptions.MissingCustomViewConfigurationException;
import ua.com.fielden.platform.web.ioc.exceptions.MissingMasterConfigurationException;
import ua.com.fielden.platform.web.ioc.exceptions.MissingWebResourceException;
import ua.com.fielden.platform.web.resources.webui.TgAppActionsResource;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.MasterInfoProvider;

import java.io.InputStream;
import java.util.*;

import static com.google.common.base.Charsets.UTF_8;
import static java.lang.String.format;
import static java.util.Collections.sort;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.regex.Pattern.quote;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static ua.com.fielden.platform.basic.config.Workflows.deployment;
import static ua.com.fielden.platform.basic.config.Workflows.vulcanizing;
import static ua.com.fielden.platform.serialisation.api.SerialiserEngines.JACKSON;
import static ua.com.fielden.platform.utils.ResourceLoader.getStream;
import static ua.com.fielden.platform.utils.ResourceLoader.getText;
import static ua.com.fielden.platform.utils.StreamUtils.distinct;
import static ua.com.fielden.platform.web.factories.webui.ResourceFactoryUtils.*;
import static ua.com.fielden.platform.web.resources.webui.FileResource.generateFileName;

/// [IWebResourceLoader] implementation.
///
@Singleton
public class WebResourceLoader implements IWebResourceLoader {
    public static final String ERR_UNKNOWN_URI = "URI is unknown: [%s].";
    public static final String ERR_BLANK_URI = "Blank URI is invalid.";

    private final IWebUiConfig webUiConfig;
    private final ISerialiser serialiser;
    private static final Logger logger = LogManager.getLogger(WebResourceLoader.class);
    private final boolean deploymentMode;
    private final boolean vulcanizingMode;

    @Inject
    public WebResourceLoader(final IWebUiConfig webUiConfig, final ISerialiser serialiser) {
        this.webUiConfig = webUiConfig;
        this.serialiser = serialiser;
        final Workflows workflow = this.webUiConfig.workflow();
        this.deploymentMode = deployment.equals(workflow);
        this.vulcanizingMode = vulcanizing.equals(workflow);
        logger.info(format("\t[%s MODE]", vulcanizingMode ? "VULCANIZING (uses DEVELOPMENT internally)" : deploymentMode ? "DEPLOYMENT" : "DEVELOPMENT"));
    }

    @Override
    public Optional<String> loadSource(final String resourceURI) {
        return getSource(resourceURI);
    }

    @Override
    public InputStream loadStream(final String resourceUri) {
        if (StringUtils.isBlank(resourceUri)) {
            throw new MissingWebResourceException(ERR_BLANK_URI);
        }
        return ofNullable(getStream(resourceUri)).orElseThrow(() -> new MissingWebResourceException(ERR_UNKNOWN_URI.formatted(resourceUri)));
    }

    private Optional<String> getSource(final String resourceUri) {
        if (StringUtils.isBlank(resourceUri)) {
            throw new MissingWebResourceException(ERR_BLANK_URI);
        }

        if ("/app/application-startup-resources.js".equalsIgnoreCase(resourceUri)) {
            return getApplicationStartupResourcesSource(webUiConfig);
        } else if ("/app/tg-app-index.html".equalsIgnoreCase(resourceUri)) {
            return injectServiceWorkerScriptInto(webUiConfig.genAppIndex());
        } else if ("/app/logout.html".equalsIgnoreCase(resourceUri)) {
            return getFileSource("/resources/logout.html", webUiConfig.resourcePaths()).map(src -> StringUtils.replace(src, "@title", "Logout"));
        } else if ("/app/login-initiate-reset.html".equalsIgnoreCase(resourceUri)) {
            return getFileSource("/resources/login-initiate-reset.html", webUiConfig.resourcePaths()).map(src -> StringUtils.replace(src, "@title", "Login Reset Request"));
        } else if ("/app/tg-app-config.js".equalsIgnoreCase(resourceUri)) {
            return ofNullable(webUiConfig.genWebUiPreferences());
        } else if ("/app/tg-app.js".equalsIgnoreCase(resourceUri)) {
            return ofNullable(webUiConfig.genMainWebUIComponent());
        } else if ("/app/tg-reflector.js".equalsIgnoreCase(resourceUri)) {
            return getReflectorSource(webUiConfig, serialiser, (TgJackson) serialiser.getEngine(JACKSON));
        } else if (TgAppActionsResource.PATH.equalsIgnoreCase(resourceUri)) {
            return getAppActionsSource();
        } else if (resourceUri.startsWith("/master_ui")) {
            return ofNullable(getMasterSource(resourceUri.replaceFirst(quote("/master_ui/"), "").replaceFirst(quote(".js"), ""), webUiConfig));
        } else if (resourceUri.startsWith("/centre_ui")) {
            return ofNullable(getCentreSource(resourceUri.replaceFirst(quote("/centre_ui/"), "").replaceFirst(quote(".js"), ""), webUiConfig));
        } else if (resourceUri.startsWith("/custom_view")) {
            return ofNullable(getCustomViewSource(resourceUri.replaceFirst("/custom_view/", ""), webUiConfig));
        } else if (resourceUri.startsWith("/resources/")) {
            return getFileSource(resourceUri, webUiConfig.resourcePaths());
        } else {
            final String msg = ERR_UNKNOWN_URI.formatted(resourceUri);
            logger.error(msg);
            return empty();
        }
    }

    @Override
    public Optional<String> checksum(final String resourceUri) {
        if (StringUtils.isBlank(resourceUri)) {
            throw new MissingWebResourceException(ERR_BLANK_URI);
        }

        return webUiConfig.checksum(resourceUri);
    }

    /// Generates 'tg-reflector' resource with type table containing master configurations.
    ///
    /// @param webUiConfig a WebUI configuration containing information about all entity masters
    ///
    private static Optional<String> getReflectorSource(final IWebUiConfig webUiConfig, final ISerialiser serialiser, final TgJackson tgJackson) {
        final Optional<String> originalSource = ofNullable(getText("ua/com/fielden/platform/web/reflection/tg-reflector.js"));
        return originalSource.map(src -> src.replace("@typeTable", new String(serialiser.serialise(enhanceWithMasterInfo(webUiConfig, tgJackson.getTypeTable()), JACKSON), UTF_8)));
    }

    private Optional<String> getAppActionsSource() {
        return ofNullable(getText("ua/com/fielden/platform/web/app/tg-app-actions-template.js"))
                .map(src -> {
                    final var actionsCode = distinct(StreamUtils.concat(webUiConfig.getCentres().values().stream().flatMap(EntityCentre::streamActionConfigs),
                                                                        webUiConfig.getMasters().values().stream().flatMap(EntityMaster::streamActions),
                                                                        webUiConfig.getExtraActions().stream(),
                                                                        webUiConfig.configDesktopMainMenu().streamActionConfigs(),
                                                                        webUiConfig.configMobileMainMenu().streamActionConfigs())
                                                             .filter(action -> action.actionIdentifier.isPresent()),
                                                     action -> action.actionIdentifier.get())
                                            .map(action -> "'%s': %s".formatted(action.actionIdentifier.get(), FunctionalActionElement.createActionObjectForTgAppActions(action)))
                                            .collect(joining(",\n", "{\n", "\n}"));
                    return src.replace("@actions", actionsCode);
                });
    }

    /// Extends types in `typeTable` with information about their masters.
    /// The type can have no master -- in this case [#get_entityMaster()] will be empty (`null`).
    ///
    /// @param webUiConfig a WebUI configuration containing information about all entity masters
    ///
    private static Map<String, EntityType> enhanceWithMasterInfo(final IWebUiConfig webUiConfig, final Map<String, EntityType> typeTable) {
        final MasterInfoProvider masterInfoProvider = new MasterInfoProvider(webUiConfig);
        typeTable.forEach((typeName, entityType) -> {
            entityType.set_entityMaster(masterInfoProvider.getMasterInfo(typeName));
            entityType.set_newEntityMaster(masterInfoProvider.getNewEntityMasterInfo(typeName));
        });
        return typeTable;
    }

    /// Injects service worker registration script with lazy tags loading after sw registration (deployment mode).
    /// Injects lazy tags loading (development mode).
    ///
    private Optional<String> injectServiceWorkerScriptInto(final String originalSource) {
        return ofNullable(originalSource.replace("@service-worker",
                          this.deploymentMode
                          ? // deployment?
                          "        if ('serviceWorker' in navigator) {\n" +
                          "            navigator.serviceWorker.register('/service-worker.js').then(function (registration) {\n" +
                          "                if (registration.active) {\n" +
                          "                    loadTags();\n" +
                          "                } else {\n" +
                          "                    registration.onupdatefound = function () {\n" +
                          "                        const installingWorker = registration.installing;\n" +
                          "                        installingWorker.onstatechange = function () {\n" +
                          "                            if (installingWorker.state === 'activated') {\n" +
                          "                                loadTags();\n" +
                          "                            }\n" +
                          "                        };\n" +
                          "                    };\n" +
                          "                }\n" +
                          "            });\n" +
                          "        }\n"
                          : // development?
                          "        loadTags();\n"
                        ));
    }

    private Optional<String> getApplicationStartupResourcesSource(final IWebUiConfig webUiConfig) {
        return getFileSource("/resources/application-startup-resources.js", webUiConfig.resourcePaths())
               .map(src -> vulcanizingMode || deploymentMode ? appendMastersAndCentresImportURIs(src, webUiConfig) : src);
    }

    /// Appends the import URIs for all masters / centres, registered in WebUiConfig, that were not already included in <code>source</code>.
    ///
    private static String appendMastersAndCentresImportURIs(final String source, final IWebUiConfig webUiConfig) {
        final StringBuilder sb = new StringBuilder();
        sb.append(source);

        final Comparator<Class<?>> classComparator = new Comparator<Class<?>>() {
            @Override
            public int compare(final Class<?> class1, final Class<?> class2) {
                return class1.getName().compareTo(class2.getName());
            }
        };

        sb.append("\n\n/* GENERATED MASTERS FROM IWebUiConfig */\n");
        final List<Class<? extends AbstractEntity<?>>> sortedMasterTypes = new ArrayList<>(webUiConfig.getMasters().keySet());
        sort(sortedMasterTypes, classComparator); // sort types by name to provide predictable order inside vulcanized resources
        for (final Class<? extends AbstractEntity<?>> masterEntityType : sortedMasterTypes) {
            if (!alreadyIncluded(masterEntityType.getName(), source)) {
                sb.append(format("import '/master_ui/%s.js';\n", masterEntityType.getName()));
            }
        }

        sb.append("\n/* GENERATED CENTRES FROM IWebUiConfig */\n");
        final List<Class<? extends MiWithConfigurationSupport<?>>> sortedCentreTypes = new ArrayList<>(webUiConfig.getCentres().keySet());
        sort(sortedCentreTypes, classComparator); // sort types by name to provide predictable order inside vulcanized resources
        for (final Class<? extends MiWithConfigurationSupport<?>> centreMiType : sortedCentreTypes) {
            if (!alreadyIncluded(centreMiType.getName(), source)) {
                sb.append(format("import '/centre_ui/%s.js';\n", centreMiType.getName()));
            }
        }

        return sb.toString();
    }

    /// Checks whether the master or centre, associated with type `name`, was already included in `application-startup-resources` file with `source`.
    ///
    private static boolean alreadyIncluded(final String name, final String source) {
        return source.contains(name);
    }

    private static String getMasterSource(final String entityTypeString, final IWebUiConfig webUiConfig) {
        final EntityMaster<? extends AbstractEntity<?>> master = getEntityMaster(entityTypeString, webUiConfig);
        if (master == null) {
            throw new MissingMasterConfigurationException(format("The entity master configuration for %s entity is missing", entityTypeString));
        }
        return master.render().toString();
    }

    private static String getCentreSource(final String mitypeString, final IWebUiConfig webUiConfig) {
        // At this stage (#231) we only support single EntityCentre instance for both MOBILE / DESKTOP applications.
        // This means that starting the MOBILE or DESKTOP app for the first time will show us the same initial full-blown (aka-desktop)
        // configuration; the user however could change the number of columns, resize their widths etc. for MOBILE and DESKTOP apps separately
        // (see CentreUpdater.deviceSpecific method for more details).

        // In future potentially we would need to define distinct initial configurations for MOBILE and DESKTOP apps.
        // Here we would need to take device specific instance.
        final EntityCentre<? extends AbstractEntity<?>> centre = getEntityCentre(mitypeString, webUiConfig);
        if (centre == null) {
            throw new MissingCentreConfigurationException(format("The entity centre configuration for %s menu item is missing", mitypeString));
        }
        return centre.buildFor().render().toString();
    }

    private static String getCustomViewSource(final String viewName, final IWebUiConfig webUiConfig) {
        final AbstractCustomView view = getCustomView(viewName, webUiConfig);
        if (view == null) {
            throw new MissingCustomViewConfigurationException(format("The %s custom view is missing", viewName));
        }
        return view.build().render().toString();
    }

    ////////////////////////////////// Getting file source //////////////////////////////////
    private static Optional<String> getFileSource(final String resourceURI, final List<String> resourcePaths) {
        final String originalPath = resourceURI.replaceFirst("/resources/", "");
        final String filePath = generateFileName(resourcePaths, originalPath);
        if (isEmpty(filePath)) {
            logger.error(format("The requested resource (%s) wasn't found.", originalPath));
            return empty();
        } else {
            return getFileSource(filePath);
        }
    }

    private static Optional<String> getFileSource(final String filePath) {
        return ofNullable(getText(filePath));
    }

}
