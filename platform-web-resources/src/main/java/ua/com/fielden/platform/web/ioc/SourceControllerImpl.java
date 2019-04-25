package ua.com.fielden.platform.web.ioc;

import static com.google.common.base.Charsets.UTF_8;
import static java.lang.String.format;
import static java.util.Collections.sort;
import static java.util.regex.Pattern.quote;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static ua.com.fielden.platform.basic.config.Workflows.deployment;
import static ua.com.fielden.platform.basic.config.Workflows.vulcanizing;
import static ua.com.fielden.platform.serialisation.api.SerialiserEngines.JACKSON;
import static ua.com.fielden.platform.utils.ResourceLoader.getText;
import static ua.com.fielden.platform.web.factories.webui.ResourceFactoryUtils.getCustomView;
import static ua.com.fielden.platform.web.factories.webui.ResourceFactoryUtils.getEntityCentre;
import static ua.com.fielden.platform.web.factories.webui.ResourceFactoryUtils.getEntityMaster;
import static ua.com.fielden.platform.web.interfaces.DeviceProfile.DESKTOP;
import static ua.com.fielden.platform.web.interfaces.DeviceProfile.MOBILE;
import static ua.com.fielden.platform.web.resources.webui.FileResource.generateFileName;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Period;

import com.google.common.base.Charsets;
import com.google.inject.Inject;

import ua.com.fielden.platform.basic.config.Workflows;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.api.SerialiserEngines;
import ua.com.fielden.platform.serialisation.api.impl.TgJackson;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.app.ISourceController;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.custom_view.AbstractCustomView;
import ua.com.fielden.platform.web.factories.webui.ResourceFactoryUtils;
import ua.com.fielden.platform.web.interfaces.DeviceProfile;
import ua.com.fielden.platform.web.ioc.exceptions.MissingCentreConfigurationException;
import ua.com.fielden.platform.web.ioc.exceptions.MissingCustomViewConfigurationException;
import ua.com.fielden.platform.web.ioc.exceptions.MissingMasterConfigurationException;
import ua.com.fielden.platform.web.resources.webui.FileResource;
import ua.com.fielden.platform.web.view.master.EntityMaster;

/**
 * {@link ISourceController} implementation.
 *
 * @author TG Team
 *
 */
public class SourceControllerImpl implements ISourceController {
    private static final String COMMENT_END = "*/";
    private static final int COMMENT_END_LENGTH = COMMENT_END.length();
    
    private static final String COMMENT_START = "/*";
    private static final int COMMENT_START_LENGTH = COMMENT_START.length();
    
    private static final String SGL_QUOTED_IMPORT = "import '";
    private static final String DBL_QUOTED_IMPORT = "import \"";
    private static final int IMPORT_LENGTH = DBL_QUOTED_IMPORT.length();
    
    private final IWebUiConfig webUiConfig;
    private final ISerialiser serialiser;
    private final TgJackson tgJackson;
    private static final Logger logger = Logger.getLogger(SourceControllerImpl.class);
    private final LinkedHashMap<Pair<DeviceProfile, String>, LinkedHashSet<String>> dependenciesByURI = new LinkedHashMap<>();
    /**
     * All URIs (including derived ones), that will be preloaded during *-index.html loading (deviceProfile-related).
     */
    private final Map<DeviceProfile, LinkedHashSet<String>> preloadedResourcesByProfile;
    private final boolean deploymentMode;
    private final boolean vulcanizingMode;
    
    @Inject
    public SourceControllerImpl(final IWebUiConfig webUiConfig, final ISerialiser serialiser) {
        this.webUiConfig = webUiConfig;
        this.serialiser = serialiser;
        this.tgJackson = (TgJackson) serialiser.getEngine(JACKSON);
        final Workflows workflow = this.webUiConfig.workflow();
        this.deploymentMode = deployment.equals(workflow);
        this.vulcanizingMode = vulcanizing.equals(workflow);
        logger.info(String.format("\t[%s MODE]", vulcanizingMode ? "VULCANIZING (uses DEVELOPMENT internally)" : deploymentMode ? "DEPLOYMENT" : "DEVELOPMENT"));
        this.preloadedResourcesByProfile = calculatePreloadedResourcesByProfile();
        this.dependenciesByURI.clear();
    }
    
    /**
     * Reads the source and extracts the list of top-level (root) dependency URIs.
     *
     * @param source
     * @return
     */
    private static LinkedHashSet<String> getRootDependencies(final String source, final LinkedHashSet<String> currentRootDependencies) {
        final int commentStart = source.indexOf(COMMENT_START); // FIXME what about // comments?
        // TODO enhance the logic to support whitespaces etc.?
        final int doubleQuotedStart = source.indexOf(DBL_QUOTED_IMPORT);
        final int singleQuotedStart = source.indexOf(SGL_QUOTED_IMPORT);
        
        final boolean doubleQuotedPresent = doubleQuotedStart >= 0;
        final boolean singleQuotedPresent = singleQuotedStart >= 0;
        if (doubleQuotedPresent || singleQuotedPresent) {
            final boolean bothTypesPresent = doubleQuotedPresent && singleQuotedPresent;
            final boolean doubleQuoted = bothTypesPresent ? (doubleQuotedStart < singleQuotedStart) : doubleQuotedPresent;
            final int start = doubleQuoted ? doubleQuotedStart : singleQuotedStart;
            if (commentStart >= 0) {
                if (commentStart < start) {
                    // remove comment and process the rest of source
                    final String temp = source.substring(commentStart + COMMENT_START_LENGTH);
                    final int indexOfUncommentedPart = temp.indexOf(COMMENT_END);
                    final String sourceWithoutComment = temp.substring(indexOfUncommentedPart + COMMENT_END_LENGTH);
                    return getRootDependencies(sourceWithoutComment, currentRootDependencies);
                } else {
                    return rootDependencies0(source, currentRootDependencies, start, doubleQuoted);
                }
            } else {
                return rootDependencies0(source, currentRootDependencies, start, doubleQuoted);
            }
        } else {
            return currentRootDependencies;
        }
    }
    
    private static LinkedHashSet<String> rootDependencies0(final String source, final LinkedHashSet<String> currentRootDependencies, final int start, final boolean doubleQuote) {
        // process the rest of source
        final int startOfURI = start + IMPORT_LENGTH;
        final String nextCurr = source.substring(startOfURI);
        final int endOfURIIndex = doubleQuote ? nextCurr.indexOf("\"") : nextCurr.indexOf("'");
        final String importURI = nextCurr.substring(0, endOfURIIndex);
        final LinkedHashSet<String> set = new LinkedHashSet<>(currentRootDependencies);
        set.add(importURI);
        return getRootDependencies(nextCurr.substring(endOfURIIndex), set);
    }
    
    /**
     * Returns dependent resources URIs for the specified resource's 'resourceURI'.
     *
     * @return
     */
    private LinkedHashSet<String> getRootDependenciesFor(final String absolutePath, final DeviceProfile deviceProfile) {
        final Pair<DeviceProfile, String> key = Pair.pair(deviceProfile, absolutePath);
        if (!dependenciesByURI.containsKey(key)) {
            dependenciesByURI.put(key, calculateRootDependenciesFor(getSource(absolutePath, deviceProfile)));
        }
        return dependenciesByURI.get(key);
    }
    
    private LinkedHashSet<String> calculateRootDependenciesFor(final String source) {
        if (source == null) {
            return new LinkedHashSet<>();
        } else {
            final LinkedHashSet<String> dependentResourceURIs = getRootDependencies(source, new LinkedHashSet<>());
            return dependentResourceURIs;
        }
    }
    
    /**
     * Returns dependent resources URIs including transitive.
     *
     * @return
     */
    private LinkedHashSet<String> getAllDependenciesFor(final String previousPath, final String resourceURI, final DeviceProfile deviceProfile, final String tab) {
        final String absolutePath = calculateAbsoluteURI(resourceURI, previousPath);
        final LinkedHashSet<String> roots = getRootDependenciesFor(absolutePath, deviceProfile);
        final String currentPath = absolutePath.substring(0, absolutePath.lastIndexOf("/") + 1);
        final LinkedHashSet<String> all = new LinkedHashSet<>();
        for (final String root : roots) {
            final LinkedHashSet<String> rootDependencies = getAllDependenciesFor(currentPath, root, deviceProfile, tab + " ");
            all.add(calculateAbsoluteURI(root, currentPath));
            all.addAll(rootDependencies);
        }
        return all;
    }
    
    private String calculateAbsoluteURI(final String root, final String currentPath) {
        return isRelative(root) ? merge(currentPath, root) : root;
    }
    
    private boolean isRelative(final String root) {
        return !root.equals("#a")
                && !root.equals("#c")
                && !root.startsWith("/master_ui/")
                && !root.startsWith("/centre_ui/")
                && !root.startsWith("/app/")
                && !root.startsWith("/resources/")
                && !root.startsWith("http");
    }
    
    @Override
    public String loadSource(final String resourceURI, final DeviceProfile deviceProfile) {
        return enhanceSource(getSource(resourceURI, deviceProfile), deviceProfile);
    }
    
    @Override
    public String loadSourceWithFilePath(final String filePath, final DeviceProfile deviceProfile) {
        final String source = getFileSource(filePath);
        return isVulcanized(filePath) ? source : enhanceSource(source, deviceProfile);
    }
    
    /**
     * Returns <code>true</code> where the <code>filePath</code> represents the vulcanized resource (which is not needed to be analyzed for preloaded dependencies),
     * <code>false</code> otherwise.
     *
     * @param filePath
     * @return
     */
    private static boolean isVulcanized(final String filePath) {
        // covers three cases: desktop-startup-resources-vulcanized.js, mobile-startup-resources-vulcanized.js and login-startup-resources-vulcanized.js
        return filePath.endsWith("-startup-resources-vulcanized.js");
    }
    
    @Override
    public InputStream loadStreamWithFilePath(final String filePath) {
        return ResourceLoader.getStream(filePath);
    }
    
    private String enhanceSource(final String source, final DeviceProfile deviceProfile) {
        // There is a try to get the resource.
        //
        // If this is the deployment mode -- need to calculate all preloaded resources (if not calculated yet)
        //  and then exclude all preloaded resources from the requested resource file.
        //
        // If this is the development mode -- do nothing (no need to calculate all preloaded resources).
        if (!deploymentMode) {
            return source;
        } else {
            return removePrealodedDependencies(source, deviceProfile);
        }
    }
    
    /**
     * Removes preloaded dependencies from source.
     *
     * @param source
     * @param deviceProfile
     *
     * @return
     */
    private String removePrealodedDependencies(final String source, final DeviceProfile deviceProfile) {
        String result = source;
        for (final String preloaded : preloadedResourcesByProfile.get(deviceProfile)) {
            result = removePrealodedDependency(result, preloaded);
        }
        return result;
    }
    
    /**
     * Removes preloaded dependency from source.
     *
     * @param source
     * @param dependency
     *
     * @return
     */
    private String removePrealodedDependency(final String source, final String dependency) {
        // TODO VERY FRAGILE APPROACH!
        // TODO VERY FRAGILE APPROACH!
        // TODO VERY FRAGILE APPROACH! please, provide better implementation (whitespaces, ' or ", ;, etc.):
        final String replacedOurDependency = source
                .replace("import \"" + dependency + "\";", "")
                .replace("import '" + dependency + "';", "")
                .replace("import \"" + dependency + "\"", "")
                .replace("import '" + dependency + "'", "");
        
        // let's replace inner polymer dependencies:
        if (dependency.startsWith("/resources/polymer/")) {
            final String polymerDependencyFileName = dependency.substring(dependency.lastIndexOf("/") + 1);
            final String replacedPolymerDependency = replacedOurDependency
                    .replaceAll("import \".*" + polymerDependencyFileName + "\";", "")
                    .replaceAll("import '.*" + polymerDependencyFileName + "';", "")
                    .replaceAll("import \".*" + polymerDependencyFileName + "\"", "")
                    .replaceAll("import '.*" + polymerDependencyFileName + "'", "");
            return replacedPolymerDependency;
        } else {
            return replacedOurDependency;
        }
    }
    
    private EnumMap<DeviceProfile, LinkedHashSet<String>> calculatePreloadedResourcesByProfile() {
        final EnumMap<DeviceProfile, LinkedHashSet<String>> result = new EnumMap<>(DeviceProfile.class);
        result.put(DESKTOP, calculatePreloadedResources("/resources/desktop-startup-resources-origin.js", DESKTOP));
        result.put(MOBILE, calculatePreloadedResources("/resources/mobile-startup-resources-origin.js", MOBILE));
        return result;
    }
    
    private LinkedHashSet<String> calculatePreloadedResources(final String startupResourcesOrigin, final DeviceProfile deviceProfile) {
        logger.info("======== Calculating " + deviceProfile + " preloaded resources... ========");
        final DateTime start = new DateTime();
        final LinkedHashSet<String> all = getAllDependenciesFor("/", startupResourcesOrigin, deviceProfile, "");
        logger.info("\t ==> " + all + ".");
        final Period pd = new Period(start, new DateTime());
        logger.info("-------- Calculated " + deviceProfile + " preloaded resources [" + all.size() + "]. Duration [" + pd.getMinutes() + " m " + pd.getSeconds() + " s " + pd.getMillis() + " ms]. --------");
        return all;
    }
    
    private String getSource(final String resourceURI, final DeviceProfile deviceProfile) {
        if ("/app/desktop-application-startup-resources.js".equalsIgnoreCase(resourceURI)) {
            return getDesktopApplicationStartupResourcesSource(webUiConfig, this);
        } else if ("/app/tg-app-index.html".equalsIgnoreCase(resourceURI)) {
            return getTgAppIndexSource(webUiConfig, deviceProfile);
        } else if ("/app/tg-app-config.js".equalsIgnoreCase(resourceURI)) {
            return getTgAppConfigSource(webUiConfig, deviceProfile);
        } else if ("/app/tg-app.js".equalsIgnoreCase(resourceURI)) {
            return getTgAppSource(webUiConfig, deviceProfile);
        } else if ("/app/tg-reflector.js".equalsIgnoreCase(resourceURI)) {
            return getReflectorSource(serialiser, tgJackson);
        } else if (resourceURI.startsWith("/master_ui")) {
            return getMasterSource(resourceURI.replaceFirst(quote("/master_ui/"), "").replaceFirst(quote(".js"), ""), webUiConfig);
        } else if (resourceURI.startsWith("/centre_ui")) {
            return getCentreSource(resourceURI.replaceFirst(quote("/centre_ui/"), "").replaceFirst(quote(".js"), ""), webUiConfig, deviceProfile);
        } else if (resourceURI.startsWith("/custom_view")) {
            return getCustomViewSource(resourceURI.replaceFirst("/custom_view/", ""), webUiConfig);
        } else if (resourceURI.startsWith("/resources/")) {
            return getFileSource(resourceURI, webUiConfig.resourcePaths());
        } else {
            logger.error("The URI is not known: [" + resourceURI + "].");
            return null;
        }
    }
    
    /**
     * Merges the current path, in which we are doing dependency analysis, with the 'uri' (perhaps relative).
     *
     * @param currentPath
     * @param uri
     * @return
     */
    private String merge(final String currentPath, final String uri) {
        return (currentPath == null || !isRelative(uri)) ? uri :
                uri.startsWith("../") ? merge(currentPathWithoutLast(currentPath), uri.substring(3)) :
                uri.startsWith("./") ? merge(currentPath, uri.substring(2)) :
                uri.contains("/") ? merge(currentPathWith(currentPath, uri.substring(0, uri.indexOf("/"))), uri.substring(uri.indexOf("/") + 1))
                        : currentPath + uri;
    }
    
    private String currentPathWithoutLast(final String currentPath) {
        final String withLastSlash = currentPath.substring(0, currentPath.length() - 1);
        return withLastSlash.substring(0, withLastSlash.lastIndexOf("/") + 1);
    }
    
    private String currentPathWith(final String currentPath, final String suffix) {
        return currentPath + suffix + "/";
    }
    
    private static String getTgAppIndexSource(final IWebUiConfig app, final DeviceProfile deviceProfile) {
        return DESKTOP.equals(deviceProfile) ? app.genDesktopAppIndex() : app.genMobileAppIndex();
    }
    
    private static String getTgAppConfigSource(final IWebUiConfig app, final DeviceProfile deviceProfile) {
        return app.genWebUiPreferences(deviceProfile);
    }
    
    private static String getTgAppSource(final IWebUiConfig app, final DeviceProfile deviceProfile) {
        return DESKTOP.equals(deviceProfile) ? app.genDesktopMainWebUIComponent() : app.genMobileMainWebUIComponent();
    }
    
    private static String getReflectorSource(final ISerialiser serialiser, final TgJackson tgJackson) {
        final String typeTableRepresentation = new String(serialiser.serialise(tgJackson.getTypeTable(), JACKSON), UTF_8);
        final String originalSource = getText("ua/com/fielden/platform/web/reflection/tg-reflector.js");
        return originalSource.replace("@typeTable", typeTableRepresentation);
    }
    
    private static String getDesktopApplicationStartupResourcesSource(final IWebUiConfig webUiConfig, final SourceControllerImpl sourceControllerImpl) {
        final String source = getFileSource("/resources/desktop-application-startup-resources.js", webUiConfig.resourcePaths());
        if (sourceControllerImpl.vulcanizingMode || sourceControllerImpl.deploymentMode) {
            final String sourceWithMastersAndCentres = appendMastersAndCentresImportURIs(source, webUiConfig);
            logger.debug("========================================= desktop-application-startup-resources WITH MASTERS AND CENTRES =============================================");
            logger.debug(sourceWithMastersAndCentres);
            logger.debug("========================================= desktop-application-startup-resources WITH MASTERS AND CENTRES [END] =============================================");
            return sourceWithMastersAndCentres;
        } else {
            logger.debug("========================================= desktop-application-startup-resources =============================================");
            logger.debug(source);
            logger.debug("========================================= desktop-application-startup-resources [END] =============================================");
            return source;
        }
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
     * Checks whether the master or centre, associated with type <code>name</code>, was already included in 'desktop-application-startup-resources' file with <code>source</code>.
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
    
    private static String getCentreSource(final String mitypeString, final IWebUiConfig webUiConfig, final DeviceProfile device) {
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
        return centre.buildFor(device).render().toString();
    }
    
    private static String getCustomViewSource(final String viewName, final IWebUiConfig webUiConfig) {
        final AbstractCustomView view = getCustomView(viewName, webUiConfig);
        if (view == null) {
            throw new MissingCustomViewConfigurationException(format("The %s custom view is missing", viewName));
        }
        return view.build().render().toString();
    }
    
    ////////////////////////////////// Getting file source //////////////////////////////////
    private static String getFileSource(final String resourceURI, final List<String> resourcePaths) {
        final String rest = resourceURI.replaceFirst("/resources/", "");
        final int lastDotIndex = rest.lastIndexOf(".");
        final String originalPath = rest.substring(0);
        final String extension = rest.substring(lastDotIndex + 1);
        return getFileSource(originalPath, extension, resourcePaths);
    }
    
    private static String getFileSource(final String originalPath, final String extension, final List<String> resourcePaths) {
        final String filePath = generateFileName(resourcePaths, originalPath, extension);
        if (isEmpty(filePath)) {
            logger.error(format("The requested resource (%s + %s) wasn't found.", originalPath, extension));
            return null;
        } else {
            return getFileSource(filePath);
        }
    }
    
    private static String getFileSource(final String filePath) {
        return getText(filePath);
    }
    
}