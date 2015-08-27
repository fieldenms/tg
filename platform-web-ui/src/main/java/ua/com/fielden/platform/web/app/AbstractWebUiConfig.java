package ua.com.fielden.platform.web.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.app.config.IWebUiBuilder;
import ua.com.fielden.platform.web.app.config.WebUiBuilder;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.menu.IMainMenuBuilder;
import ua.com.fielden.platform.web.menu.impl.MainMenuBuilder;
import ua.com.fielden.platform.web.view.master.EntityMaster;

/**
 * The base implementation for Web UI configuration, which should be inherited from in concrete applications for defining the final application specific Web UI configuration.
 * <p>
 * Method {@link IWebUiConfig#initConfiguration()} should be implemented in the application specific Web UI configuration, where menus, entity centres and entity master should be registered
 * by obtaining corresponding builders via methods {@link #configApp()} and {@link #configMainMenu()}.
 *
 * @author TG Team
 *
 */
public abstract class AbstractWebUiConfig implements IWebUiConfig {

    private final String title;
    private final WebUiBuilder webUiBuilder;
    private final MainMenuBuilder mainMenuConfig;
    private Injector injector;
    /**
     * The paths for any kind of file resources those are needed for browser client. These are mapped to the '/resources/' router path. Also these resource paths might be augmented
     * with other custom paths. When client asks for a resource then this application will search for that resource in these paths starting from the custom ones.
     */
    private final List<String> resourcePaths;

    /**
     * Creates abstract {@link IWebUiConfig}.
     *
     * @param title
     * @param externalResourcePaths
     * - additional root paths for file resources. (see {@link #resourcePaths} for more information).
     */
    public AbstractWebUiConfig(final String title, final String[] externalResourcePaths) {
        this.title = title;
        this.webUiBuilder = new WebUiBuilder(this);
        this.mainMenuConfig = new MainMenuBuilder(this);

        final LinkedHashSet<String> allResourcePaths = new LinkedHashSet<>();
        allResourcePaths.addAll(Arrays.asList("", "ua/com/fielden/platform/web/"));
        allResourcePaths.addAll(Arrays.asList(externalResourcePaths));
        this.resourcePaths = new ArrayList<String>(Collections.unmodifiableSet(allResourcePaths));
        Collections.reverse(this.resourcePaths);
    }

    @Override
    public IWebUiBuilder configApp() {
        return webUiBuilder;
    }

    @Override
    public IMainMenuBuilder configMainMenu() {
        return mainMenuConfig;
    }

    /**
     * Generates the global configuration component.
     *
     * @return
     */
    @Override
    public final String genWebUiPreferences() {
        return webUiBuilder.genWebUiPrefComponent();
    }

    /**
     * Generates the main menu component.
     *
     * @return
     */
    @Override
    public final String genMainWebUIComponent() {
        return ResourceLoader.getText("ua/com/fielden/platform/web/app/tg-app.html").
                replaceAll("@menuConfig", mainMenuConfig.code().toString());
    }

    /**
     * Generates the web application.
     *
     * @return
     */
    @Override
    public final String genAppIndex() {
        return ResourceLoader.getText("ua/com/fielden/platform/web/index.html").
                replaceAll("@title", title);
    }

    /**
     * Returns the map of entity masters for this web application.
     *
     * @return
     */
    @Override
    public final Map<Class<? extends AbstractEntity<?>>, EntityMaster<? extends AbstractEntity<?>>> getMasters() {
        return webUiBuilder.getMasters();
    }

    /**
     * Returns the map of entity centres for this web application.
     *
     * @return
     */
    @Override
    public final Map<Class<? extends MiWithConfigurationSupport<?>>, EntityCentre<?>> getCentres() {
        return webUiBuilder.getCentres();
    }

    public void setInjector(final Injector injector) {
        this.injector = injector;
    }

    protected Injector injector() {
        return injector;
    }

    @Override
    public List<String> resourcePaths() {
        return this.resourcePaths;
    }
}
