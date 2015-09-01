package ua.com.fielden.platform.web.ioc;

import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Charsets;
import com.google.inject.Inject;

import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.api.SerialiserEngines;
import ua.com.fielden.platform.serialisation.api.impl.TgJackson;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.app.ISourceController;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.factories.webui.ResourceFactoryUtils;
import ua.com.fielden.platform.web.resources.webui.FileResource;

/**
 * {@link ISourceController} implementation.
 *
 * @author TG Team
 *
 */
public class SourceControllerImpl implements ISourceController {
    private final IWebUiConfig webUiConfig;
    private final ISerialiser serialiser;
    private final TgJackson tgJackson;
    /**
     * Root URIs, that will be preloaded ('/resources/application-startup-resources.html' defines them) during index.html loading.
     */
    private LinkedHashSet<String> preloadedResources;
    /**
     * All URIs (including derived ones), that will be preloaded ('/resources/application-startup-resources.html' defines them) during index.html loading.
     */
    private LinkedHashSet<String> allPreloadedResources;
    private Boolean deploymentMode;

    @Inject
    public SourceControllerImpl(final IWebUiConfig webUiConfig, final ISerialiser serialiser) {
        this.webUiConfig = webUiConfig;
        this.serialiser = serialiser;
        this.tgJackson = (TgJackson) serialiser.getEngine(SerialiserEngines.JACKSON);
    }

    /**
     * Returns <code>true</code> in case where the server is in deployment mode, <code>false</code> -- in development mode.
     * <p>
     * At this stage deployment mode is activated in the case, where /resources/startup-resources.html and /resources/startup-resources-origin.html are different (which means that potentially /resources/startup-resources.html is vulcanized
     * version of /resources/startup-resources-original.html or just changed intentionally to activate the deployment mode for testing purposes).
     *
     * After the first heavy comparison has been performed -- the flag is cached.
     *
     * @return
     */
    public boolean isDeploymentMode() {
        if (deploymentMode == null) {
            final String startupResources = getSource("/resources/startup-resources.html");
            final String startupResourcesOrigin = getSource("/resources/startup-resources-origin.html");
            deploymentMode = !EntityUtils.equalsEx(startupResources, startupResourcesOrigin);
        }
        return deploymentMode;
    }

    public void setDeploymentMode(final boolean deploymentMode) {
        if (this.deploymentMode != null) {
            throw new IllegalStateException("The [deployment mode == " + this.deploymentMode + "] has been already automatically populated (it cannot be overriden now). Please, ensure that no resource loading occurs before this setting.");
        }
        this.deploymentMode = deploymentMode;
    }

    /**
     * Reads the source and extracts the list of top-level (root) dependency URIs.
     *
     * @param source
     * @return
     */
    private static LinkedHashSet<String> getRootDependencies(final String source) {
        final LinkedHashSet<String> set = new LinkedHashSet<String>();
        String curr = source;
        // TODO enhance the logic to support whitespaces etc.?
        while (curr.indexOf("href=\"") >= 0 || curr.indexOf("href='") >= 0) {
            final boolean singleQuote = curr.indexOf("href='") >= 0;
            final int startIndex = singleQuote ? curr.indexOf("href='") + 6 : curr.indexOf("href=\"") + 6;
            final String nextCurr = curr.substring(startIndex);
            final int endIndex = singleQuote ? nextCurr.indexOf("'") : nextCurr.indexOf("\"");
            final String importURI = nextCurr.substring(0, endIndex);
            set.add(importURI);
            curr = nextCurr.substring(endIndex);
        }

        return set;
    }

    /**
     * Returns app-specific preloaded resources.
     *
     * @return
     */
    private LinkedHashSet<String> getApplicationStartupRootDependencies() {
        if (this.preloadedResources == null) {
            this.preloadedResources = new LinkedHashSet<>();

            final LinkedHashSet<String> result = getRootDependenciesFor("/resources/application-startup-resources.html");
            if (result == null) {
                throw new IllegalStateException("The [/resources/application-startup-resources.html] resource should exist. It is crucial for startup loading of app-specific resources.");
            }

            this.preloadedResources = result;
        }
        return this.preloadedResources;
    }

    /**
     * Returns dependent resources URIs for the specified resource's 'resourceURI'.
     *
     * @return
     */
    private LinkedHashSet<String> getRootDependenciesFor(final String resourceURI) {
        final String source = getSource(resourceURI);
        if (source == null) {
            return null;
        } else {
            final LinkedHashSet<String> dependentResourceURIs = getRootDependencies(source);
            final LinkedHashSet<String> dependentResourceURIsWithoutCoreTooltip = new LinkedHashSet<>(dependentResourceURIs);
            for (final String dependentResourceURI: dependentResourceURIs) {
                if (dependentResourceURI.contains("core-tooltip")) {
                    dependentResourceURIsWithoutCoreTooltip.remove(dependentResourceURI);
                }
            }
            // System.out.println("get: [" + resourceURI + "] ==> " + dependentResourceURIsWithoutCoreTooltip);
            return dependentResourceURIsWithoutCoreTooltip;
        }
    }

    /**
     * Returns dependent resources URIs including transitive.
     *
     * @return
     */
    private LinkedHashSet<String> getAllDependenciesFor(final String resourceURI) {
        final LinkedHashSet<String> roots = getRootDependenciesFor(resourceURI);
        if (roots == null) {
            return null;
        } else {
            final LinkedHashSet<String> all = new LinkedHashSet<>();
            for (final String root : roots) {
                final LinkedHashSet<String> rootDependencies = getAllDependenciesFor(root);
                if (rootDependencies != null) {
                    all.add(root);
                    all.addAll(rootDependencies);
                } else {
                    // System.out.println("disregarded dependencies of unknown resource [" + root + "]");
                }
            }
            return all;
        }
    }

    @Override
    public String loadSource(final String resourceURI) {
        final String source = getSource(resourceURI);
        return enhanceSource(source, resourceURI);
    }

    @Override
    public String loadSourceWithFilePath(final String filePath) {
        final String source = getFileSource(filePath);
        return enhanceSource(source, filePath);
    }

    @Override
    public InputStream loadStreamWithFilePath(final String filePath) {
        return ResourceLoader.getStream(filePath);
    }

    private String enhanceSource(final String source, final String path) {
        // There is a try to get the resource.
        //
        // If this is the deployment mode -- need to calculate all preloaded resources (if not calculated yet)
        //  and then exclude all preloaded resources from the requested resource file.
        //
        // If this is the development mode -- do nothing (no need to calculate all preloaded resources).
        if (!isDeploymentMode()) {
            return source;
        } else {
            if (allPreloadedResources == null) {
                allPreloadedResources = calculatePreloadedResources();
            }

            // System.out.println("SOURCE [" + path + "]: " + source);
            final String sourceWithoutPreloadedDependencies = removePrealodedDependencies(source);
            // System.out.println("SOURCE WITHOUT PRELOADED [" + path + "]: " + sourceWithoutPreloadedDependencies);
            return sourceWithoutPreloadedDependencies;
        }
    }

    /**
     * Removes preloaded dependencies from source.
     *
     * @param source
     *
     * @return
     */
    private String removePrealodedDependencies(final String source) {
        String result = source;
        for (final String preloaded : allPreloadedResources) {
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
        // TODO VERY FRAGILE APPROACH! please, provide better implementation (whitespaces, exchanged rel and href, etc.?):
        return source.replaceAll("<link rel=\"import\" href=\"" + dependency + "\">", "")
                    .replaceAll("<link rel='import' href='" + dependency + "'>", "");
    }

    private LinkedHashSet<String> calculatePreloadedResources() {
        System.out.println("=============calculatePreloadedResources===============");
        // System.out.println("allUrls = |" + getAll("/resources/binding/tg-entity-binder.html") + "|.");
        // System.out.println("allUrls = |" + getAll("/master_ui/ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties") + "|.");
        // System.out.println("allUrls = |" + getAll("/centre_ui/ua.com.fielden.platform.sample.domain.MiTgPersistentEntityWithProperties") + "|.");
        // System.out.println("allUrls = |" + getAll("/resources/application-startup-resources.html") + "|.");
        final LinkedHashSet<String> all = getAllDependenciesFor("/resources/startup-resources-origin.html");
        System.out.println("allUrls = |" + all + "|.");
        System.out.println("--------------calculatePreloadedResources--------------");
        return all;
    }

    private String getSource(final String resourceURI) {
        if ("/app/tg-app-config.html".equalsIgnoreCase(resourceURI)) {
            return getTgAppConfigSource(webUiConfig);
        } else if ("/app/tg-app.html".equalsIgnoreCase(resourceURI)) {
            return getTgAppSource(webUiConfig);
        } else if ("/app/tg-reflector.html".equalsIgnoreCase(resourceURI)) {
            return getReflectorSource(serialiser, tgJackson);
        } else if ("/app/tg-element-loader.html".equalsIgnoreCase(resourceURI)) {
            return getElementLoaderSource(this, webUiConfig);
        } else if (resourceURI.startsWith("/master_ui")) {
            return getMasterSource(resourceURI.replaceFirst("/master_ui/", ""), webUiConfig);
        } else if (resourceURI.startsWith("/centre_ui")) {
            return getCentreSource(resourceURI.replaceFirst("/centre_ui/", ""), webUiConfig);
        } else if (resourceURI.startsWith("/resources/")) {
            return getFileSource(resourceURI, webUiConfig.resourcePaths());
        } else {
            // System.out.println("The URI is not known: [" + resourceURI + "].");
            return null;
        }
    }

    private static String getTgAppConfigSource(final IWebUiConfig app) {
        return app.genWebUiPreferences();
    }

    private static String getTgAppSource(final IWebUiConfig app) {
        return app.genMainWebUIComponent();
    }

    private static String getReflectorSource(final ISerialiser serialiser, final TgJackson tgJackson) {
        final String typeTableRepresentation = new String(serialiser.serialise(tgJackson.getTypeTable(), SerialiserEngines.JACKSON), Charsets.UTF_8);
        final String originalSource = ResourceLoader.getText("ua/com/fielden/platform/web/reflection/tg-reflector.html");

        return originalSource.replace("@typeTable", typeTableRepresentation);
    }

    private static String getElementLoaderSource(final SourceControllerImpl sourceControllerImpl, final IWebUiConfig webUiConfig) {
        final String source = getFileSource("/resources/element_loader/tg-element-loader.html", webUiConfig.resourcePaths());
        return source.replace("importedURLs = {}", generateImportUrlsFrom(sourceControllerImpl.getApplicationStartupRootDependencies()));
    }

    /**
     * Generates the string of tg-element-loader's 'importedURLs' from 'appSpecificPreloadedResources'.
     *
     * @param appSpecificPreloadedResources
     * @return
     */
    private static String generateImportUrlsFrom(final LinkedHashSet<String> appSpecificPreloadedResources) {
        final String prepender = "importedURLs = {";
        final StringBuilder sb = new StringBuilder("");
        final Iterator<String> iter = appSpecificPreloadedResources.iterator();
        while (iter.hasNext()) {
            final String next = iter.next();
            sb.append(",'" + next + "': 'imported'");
        }
        final String res = sb.toString();
        return prepender + (StringUtils.isEmpty(res) ? "" : res.substring(1)) + "}";
    }

    private static String getMasterSource(final String entityTypeString, final IWebUiConfig webUiConfig) {
        return ResourceFactoryUtils.getEntityMaster(entityTypeString, webUiConfig).build().render().toString();
    }

    private static String getCentreSource(final String mitypeString, final IWebUiConfig webUiConfig) {
        return ResourceFactoryUtils.getEntityCentre(mitypeString, webUiConfig).build().render().toString();
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
        final String filePath = FileResource.generateFileName(resourcePaths, originalPath);
        if (StringUtils.isEmpty(filePath)) {
            System.out.println("The requested resource (" + originalPath + " + " + extension + ") wasn't found.");
            return null;
        } else {
            return getFileSource(filePath);
        }
    }

    private static String getFileSource(final String filePath) {
        return ResourceLoader.getText(filePath);
    }
}
