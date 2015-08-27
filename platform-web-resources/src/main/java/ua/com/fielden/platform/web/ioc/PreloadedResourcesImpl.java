package ua.com.fielden.platform.web.ioc;

import java.util.LinkedHashSet;

import com.google.inject.Inject;

import ua.com.fielden.platform.serialisation.api.SerialiserEngines;
import ua.com.fielden.platform.serialisation.api.impl.TgJackson;
import ua.com.fielden.platform.web.app.IPreloadedResources;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.resources.MainWebUiComponentResource;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.WebUiPreferencesResource;
import ua.com.fielden.platform.web.resources.webui.CentreComponentResource;
import ua.com.fielden.platform.web.resources.webui.FileResource;
import ua.com.fielden.platform.web.resources.webui.MasterComponentResource;
import ua.com.fielden.platform.web.resources.webui.TgElementLoaderComponentResource;
import ua.com.fielden.platform.web.resources.webui.TgReflectorComponentResource;

/**
 * {@link IPreloadedResources} implementation.
 *
 * @author TG Team
 *
 */
public class PreloadedResourcesImpl implements IPreloadedResources {
    private final IWebUiConfig webUiConfig;
    private final RestServerUtil restUtil;
    private final TgJackson tgJackson;
    private LinkedHashSet<String> preloadedResources;

    @Inject
    public PreloadedResourcesImpl(final IWebUiConfig webUiConfig, final RestServerUtil restUtil) {
        this.webUiConfig = webUiConfig;
        this.restUtil = restUtil;
        this.tgJackson = (TgJackson) this.restUtil.getSerialiser().getEngine(SerialiserEngines.JACKSON);
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

            System.out.println("============================");
            // System.out.println("allUrls = |" + preloadedResources.getAll("/resources/binding/tg-entity-binder.html") + "|.");
            // System.out.println("allUrls = |" + preloadedResources.getAll("/master_ui/ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties") + "|.");
            // System.out.println("allUrls = |" + this.getAll("/centre_ui/ua.com.fielden.platform.sample.domain.MiTgPersistentEntityWithProperties") + "|.");
            // System.out.println("allUrls = |" + getAll("/resources/application-startup-resources.html") + "|.");
            System.out.println("allUrls = |" + getAll("/resources/startup-resources-origin.html") + "|.");
            System.out.println("----------------------------");


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

    @Override
    public String getSource(final String resourceURI) {
        if ("/app/tg-app-config.html".equalsIgnoreCase(resourceURI)) {
            return WebUiPreferencesResource.get(webUiConfig);
        } else if ("/app/tg-app.html".equalsIgnoreCase(resourceURI)) {
            return MainWebUiComponentResource.get(webUiConfig);
        } else if ("/app/tg-reflector.html".equalsIgnoreCase(resourceURI)) {
            return TgReflectorComponentResource.get(restUtil, tgJackson);
        } else if ("/app/tg-element-loader.html".equalsIgnoreCase(resourceURI)) {
            return TgElementLoaderComponentResource.get(this);
        } else if (resourceURI.startsWith("/master_ui")) {
            return MasterComponentResource.get(resourceURI.replaceFirst("/master_ui/", ""), webUiConfig);
        } else if (resourceURI.startsWith("/centre_ui")) {
            return CentreComponentResource.get(resourceURI.replaceFirst("/centre_ui/", ""), webUiConfig);
        } else if (resourceURI.startsWith("/resources/")) {
            final String rest = resourceURI.replaceFirst("/resources/", "");
            final int lastDotIndex = rest.lastIndexOf(".");
            final String originalPath = rest.substring(0);
            final String extension = rest.substring(lastDotIndex + 1);
            return FileResource.get(originalPath, extension, webUiConfig.resourcePaths());
        } else {
            // System.out.println("The URI is not known: [" + resourceURI + "].");
            return null;
        }
    }
}
