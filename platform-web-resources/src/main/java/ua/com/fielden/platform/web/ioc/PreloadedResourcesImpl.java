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
import ua.com.fielden.platform.web.app.IPreloadedResources;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.factories.webui.ResourceFactoryUtils;
import ua.com.fielden.platform.web.resources.MainWebUiComponentResource;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.WebUiPreferencesResource;
import ua.com.fielden.platform.web.resources.webui.FileResource;

/**
 * {@link IPreloadedResources} implementation.
 *
 * @author TG Team
 *
 */
public class PreloadedResourcesImpl implements IPreloadedResources {
    private final IWebUiConfig webUiConfig;
    private final RestServerUtil restUtil;
    private final ISerialiser serialiser;
    private final TgJackson tgJackson;
    private LinkedHashSet<String> preloadedResources;
    private LinkedHashSet<String> calculatedPreloadedResources;
    private Boolean deploymentMode;

    @Inject
    public PreloadedResourcesImpl(final IWebUiConfig webUiConfig, final RestServerUtil restUtil) {
        this.webUiConfig = webUiConfig;
        this.restUtil = restUtil;
        this.serialiser = this.restUtil.getSerialiser();
        this.tgJackson = (TgJackson) serialiser.getEngine(SerialiserEngines.JACKSON);
    }

    private boolean isDeploymentMode() {
        if (deploymentMode == null) {
            final String startupResources = getSource("/resources/startup-resources.html");
            final String startupResourcesOrigin = getSource("/resources/startup-resources-origin.html");
            deploymentMode = !EntityUtils.equalsEx(startupResources, startupResourcesOrigin);
        }
        return deploymentMode;
    }

    /**
     * Reads the source and extracts the list of top-level (root) dependency URIs.
     *
     * @param source
     * @return
     */
    private static LinkedHashSet<String> getDependentResourceURIs(final String source) {
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

    @Override
    public LinkedHashSet<String> get() {
        if (this.preloadedResources == null) {
            this.preloadedResources = new LinkedHashSet<>();

            final LinkedHashSet<String> result = get("/resources/application-startup-resources.html");
            if (result == null) {
                throw new IllegalStateException("The [/resources/application-startup-resources.html] resource should exist. It is crucial for startup loading of app-specific resources.");
            }

            this.preloadedResources = result;
        }
        return this.preloadedResources;
    }

    @Override
    public LinkedHashSet<String> get(final String resourceURI) {
        final String source = getSource(resourceURI);
        if (source == null) {
            return null;
        } else {
            final LinkedHashSet<String> dependentResourceURIs = getDependentResourceURIs(source);
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

    @Override
    public LinkedHashSet<String> getAll(final String resourceURI) {
        final LinkedHashSet<String> roots = get(resourceURI);
        if (roots == null) {
            return null;
        } else {
            final LinkedHashSet<String> all = new LinkedHashSet<>();
            for (final String root : roots) {
                final LinkedHashSet<String> rootDependencies = getAll(root);
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
            if (calculatedPreloadedResources == null) {
                calculatedPreloadedResources = calculatePreloadedResources();
            }

            // System.out.println("SOURCE [" + path + "]: " + source);
            final String sourceWithoutPreloadedDependencies = removePrealodedDependencies(source);
            // System.out.println("SOURCE WITHOUT PRELOADED [" + path + "]: " + sourceWithoutPreloadedDependencies);
            return sourceWithoutPreloadedDependencies;
        }
    }

    @Override
    public String getSourceOnTheFly(final String resourceURI) {
        final String source = getSource(resourceURI);
        return enhanceSource(source, resourceURI);
    }

    @Override
    public String getSourceOnTheFlyWithFilePath(final String filePath) {
        final String source = getFileSource(filePath);
        return enhanceSource(source, filePath);
    }

    @Override
    public InputStream getStreamOnTheFly(final String filePath) {
        return ResourceLoader.getStream(filePath);
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
        for (final String preloaded : calculatedPreloadedResources) {
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
        // System.out.println("allUrls = |" + preloadedResources.getAll("/resources/binding/tg-entity-binder.html") + "|.");
        // System.out.println("allUrls = |" + preloadedResources.getAll("/master_ui/ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties") + "|.");
        // System.out.println("allUrls = |" + this.getAll("/centre_ui/ua.com.fielden.platform.sample.domain.MiTgPersistentEntityWithProperties") + "|.");
        // System.out.println("allUrls = |" + getAll("/resources/application-startup-resources.html") + "|.");
        final LinkedHashSet<String> all = getAll("/resources/startup-resources-origin.html");
        System.out.println("allUrls = |" + all + "|.");
        System.out.println("--------------calculatePreloadedResources--------------");
        return all;
    }

    @Override
    public String getSource(final String resourceURI) {
        if ("/app/tg-app-config.html".equalsIgnoreCase(resourceURI)) {
            return WebUiPreferencesResource.get(webUiConfig);
        } else if ("/app/tg-app.html".equalsIgnoreCase(resourceURI)) {
            return MainWebUiComponentResource.get(webUiConfig);
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

    private static String getReflectorSource(final ISerialiser serialiser, final TgJackson tgJackson) {
        final String typeTableRepresentation = new String(serialiser.serialise(tgJackson.getTypeTable(), SerialiserEngines.JACKSON), Charsets.UTF_8);
        final String originalSource = ResourceLoader.getText("ua/com/fielden/platform/web/reflection/tg-reflector.html");

        return originalSource.replace("@typeTable", typeTableRepresentation);
    }

    private static String getElementLoaderSource(final IPreloadedResources preloadedResources, final IWebUiConfig webUiConfig) {
        final String source = getFileSource("/resources/element_loader/tg-element-loader.html", webUiConfig.resourcePaths());
        return source.replace("importedURLs = {}", generateImportUrlsFrom(preloadedResources.get()));
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

    public static String getMasterSource(final String entityTypeString, final IWebUiConfig webUiConfig) {
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
