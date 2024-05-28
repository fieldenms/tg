package ua.com.fielden.platform.web.ioc;

import static com.google.common.base.Charsets.UTF_8;
import static java.lang.String.format;
import static java.util.Collections.sort;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.regex.Pattern.quote;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static ua.com.fielden.platform.basic.config.Workflows.deployment;
import static ua.com.fielden.platform.basic.config.Workflows.vulcanizing;
import static ua.com.fielden.platform.serialisation.api.SerialiserEngines.JACKSON;
import static ua.com.fielden.platform.utils.ResourceLoader.getStream;
import static ua.com.fielden.platform.utils.ResourceLoader.getText;
import static ua.com.fielden.platform.web.factories.webui.ResourceFactoryUtils.getCustomView;
import static ua.com.fielden.platform.web.factories.webui.ResourceFactoryUtils.getEntityCentre;
import static ua.com.fielden.platform.web.factories.webui.ResourceFactoryUtils.getEntityMaster;
import static ua.com.fielden.platform.web.resources.webui.FileResource.generateFileName;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;

import ua.com.fielden.platform.basic.config.Workflows;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.api.impl.TgJackson;
import ua.com.fielden.platform.serialisation.jackson.EntityType;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.app.IWebResourceLoader;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.custom_view.AbstractCustomView;
import ua.com.fielden.platform.web.ioc.exceptions.MissingCentreConfigurationException;
import ua.com.fielden.platform.web.ioc.exceptions.MissingCustomViewConfigurationException;
import ua.com.fielden.platform.web.ioc.exceptions.MissingMasterConfigurationException;
import ua.com.fielden.platform.web.ioc.exceptions.MissingWebResourceException;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.MasterInfoProvider;

/**
 * {@link IWebResourceLoader} implementation.
 *
 * @author TG Team
 *
 */
public class WebResourceLoader implements IWebResourceLoader {
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
        return ofNullable(getStream(resourceUri)).orElseThrow(() -> new MissingWebResourceException(format("URI is unknown: [%s].", resourceUri)));
    }

    private Optional<String> getSource(final String resourceUri) {
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
        } else if ("/app/tg-reflector.js".equalsIgnoreCase(resourceUri)) {
            return getReflectorSource(webUiConfig, serialiser, (TgJackson) serialiser.getEngine(JACKSON));
        } else if (resourceUri.startsWith("/master_ui")) {
            return ofNullable(getMasterSource(resourceUri.replaceFirst(quote("/master_ui/"), "").replaceFirst(quote(".js"), ""), webUiConfig));
        } else if (resourceUri.startsWith("/centre_ui")) {
            return ofNullable(getCentreSource(resourceUri.replaceFirst(quote("/centre_ui/"), "").replaceFirst(quote(".js"), ""), webUiConfig));
        } else if (resourceUri.startsWith("/custom_view")) {
            return ofNullable(getCustomViewSource(resourceUri.replaceFirst("/custom_view/", ""), webUiConfig));
        } else if (resourceUri.startsWith("/resources/")) {
            return getFileSource(resourceUri, webUiConfig.resourcePaths());
        } else {
            final String msg = format("URI is unknown: [%s].", resourceUri);
            logger.error(msg);
            return empty();
        }
    }

    @Override
    public Optional<String> checksum(final String resourceURI) {
        return webUiConfig.checksum(resourceURI);
    }

    /**
     * Generates 'tg-reflector' resource with type table containing master configurations.
     *
     * @param webUiConfig -- web UI configuration containing information about all entity masters
     * @param serialiser
     * @param tgJackson
     * @return
     */
    private static Optional<String> getReflectorSource(final IWebUiConfig webUiConfig, final ISerialiser serialiser, final TgJackson tgJackson) {
        final Optional<String> originalSource = ofNullable(getText("ua/com/fielden/platform/web/reflection/tg-reflector.js"));
        return originalSource.map(src -> src.replace("@typeTable", new String(serialiser.serialise(enhanceWithMasterInfo(webUiConfig, tgJackson.getTypeTable()), JACKSON), UTF_8)));
    }

    /**
     * Extends types in {@code typeTable} with information about their masters.
     * The type can have no master -- in this case {@link EntityType#get_entityMaster()} will be empty ({@code null}).
     *
     * @param webUiConfig -- web UI configuration containing information about all entity masters
     * @param typeTable
     * @return
     */
    private static Map<String, EntityType> enhanceWithMasterInfo(final IWebUiConfig webUiConfig, final Map<String, EntityType> typeTable) {
        final MasterInfoProvider masterInfoProvider = new MasterInfoProvider(webUiConfig);
        typeTable.forEach((typeName, entityType) -> {
            entityType.set_entityMaster(masterInfoProvider.getMasterInfo(typeName));
            entityType.set_newEntityMaster(masterInfoProvider.getNewEntityMasterInfo(typeName));
        });
        return typeTable;
    }

    /**
     * Injects service worker registration script with lazy tags loading after sw registration (deployment mode).
     * Injects lazy tags loading (development mode).
     *
     * @param originalSource
     * @return
     */
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

    /**
     * Appends the import URIs for all masters / centres, registered in WebUiConfig, that were not already included in <code>source</code>.
     *
     * @param source
     * @param webUiConfig
     * @return
     */
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

    /**
     * Checks whether the master or centre, associated with type <code>name</code>, was already included in 'application-startup-resources' file with <code>source</code>.
     *
     * @param name
     * @param source
     * @return
     */
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
        final String updatePath = originalPath.lastIndexOf(".") >= 0 ? originalPath : originalPath + ".js";
        final String filePath = generateFileName(resourcePaths, updatePath);
        if (isEmpty(filePath)) {
            logger.error(format("The requested resource (%s) wasn't found.", originalPath));
            return empty();
        } else {
            return getFile(filePath, resourcePaths);
        }
    }

    private static Optional<String> getFile(final String filePath, final List<String> resourcePaths) {
        return ofNullable(replaceBareModuleSpecifier(filePath, resourcePaths));
    }

    private static String replaceBareModuleSpecifier(final String filePath, final List<String> resourcePaths) {
        final String fileText = getText(filePath);
        if (fileText != null) {
            final Pattern importWithFrom = Pattern.compile("((import|export)\\s*?[^;]+?\\s*?from\\s*?[\"'])([\\w@][\\w@//.\\-\\_]*?)([\"'];?)", Pattern.MULTILINE);
            final Pattern simpleImport = Pattern.compile("((import\\s*?)[\"'])([\\w@][\\w@//.\\-\\_]*?)(([\"']);?)", Pattern.MULTILINE);

            final String closestNodeModules = findClosestNodeModules(filePath, resourcePaths);
            final Function<MatchResult, String> nodeResolver = matchResult -> matchResult.group(1) + "/resources/" + closestNodeModules + "/"
                    + resolveModule(matchResult.group(3), closestNodeModules, resourcePaths) + matchResult.group(4);

            return importWithFrom.matcher(simpleImport.matcher(fileText).replaceAll(nodeResolver))
                    .replaceAll(nodeResolver);
        }
        return null;
    }

    private static String findClosestNodeModules(final String filePath, final List<String> resourcePaths) {
        final String parentResourcePath = findParentResourcePath(resourcePaths, filePath);
        String path = filePath.substring(parentResourcePath.length());
        while (isNotEmpty(path)  && !ResourceLoader.exist(parentResourcePath + "/" + path + "/node_modules")) {
            final int lastSlashIndex = path.lastIndexOf('/');
            path = lastSlashIndex < 0 ? "" : path.substring(0, lastSlashIndex);
        }
        return isEmpty(path) ? "node_modules" : path + "/node_modules";
    }

    private static String findParentResourcePath(final List<String> resourcePaths, final String filePath) {
        return resourcePaths.stream().filter(resourcePath -> filePath.startsWith(resourcePath)).findFirst().orElse("");
    }

    private static String resolveModule(final String moduleName, final String closestNodeModules, final List<String> resourcePaths) {
        if (moduleName.endsWith(".js")) {
            return moduleName;
        } else if (moduleName.indexOf(".") < 0){
            final String packagePath = generateFileName(resourcePaths, closestNodeModules + "/" + moduleName + "/package.json");
            if (!isEmpty(packagePath)) {
                final String packageText = getText(packagePath);
                return getProperty(getText(packagePath), "module")
                        .filter(main -> main.endsWith(".js"))
                        .or(() -> getProperty(getText(packagePath), "main"))
                        .map(mainModuleName -> moduleName + "/" + mainModuleName)
                        .orElse(moduleName + "/index.js");
            } else {
                return moduleName + "/index.js";
            }
        } else {
            return moduleName;
        }

    }

    private static Optional<String> getProperty(final String fileText, final String property) {
        if (fileText != null) {
            final Pattern pattern = Pattern.compile("['\"]?+" + property + "['\"]?+\\s*?:\\s*?[\"'](.+)[\"']", Pattern.MULTILINE);
            final Matcher matcher = pattern.matcher(fileText);
            if (matcher.find()) {
                return Optional.ofNullable(matcher.group(1));
            }
        }
        return Optional.empty();
    }
}