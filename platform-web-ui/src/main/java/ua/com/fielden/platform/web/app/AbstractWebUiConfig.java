package ua.com.fielden.platform.web.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.inject.Injector;

import ua.com.fielden.platform.basic.config.Workflows;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.menu.Menu;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.app.config.IWebUiBuilder;
import ua.com.fielden.platform.web.app.config.WebUiBuilder;
import ua.com.fielden.platform.web.centre.CentreUpdater;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.custom_view.AbstractCustomView;
import ua.com.fielden.platform.web.menu.IMainMenuBuilder;
import ua.com.fielden.platform.web.menu.impl.MainMenuBuilder;
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.EntityMaster;

/**
 * The base implementation for Web UI configuration, which should be inherited from in concrete applications for defining the final application specific Web UI configuration.
 * <p>
 * Method {@link IWebUiConfig#initConfiguration()} should be implemented in the application specific Web UI configuration, where menus, entity centres and entity master should be
 * registered by obtaining corresponding builders via methods {@link #configApp()} and {@link #configDesktopMainMenu()}.
 *
 * @author TG Team
 *
 */
public abstract class AbstractWebUiConfig implements IWebUiConfig {
    private final Logger logger = Logger.getLogger(getClass());
    private final String title;
    private WebUiBuilder webUiBuilder;
    private MainMenuBuilder desktopMainMenuConfig;
    private MainMenuBuilder mobileMainMenuConfig;
    private Injector injector;
    /**
     * The paths for any kind of file resources those are needed for browser client. These are mapped to the '/resources/' router path. Also these resource paths might be augmented
     * with other custom paths. When client asks for a resource then this application will search for that resource in these paths starting from the custom ones.
     */
    private final List<String> resourcePaths;
    private final Workflows workflow;

    /**
     * Creates abstract {@link IWebUiConfig}.
     *
     * @param title
     * @param externalResourcePaths
     * - additional root paths for file resources. (see {@link #resourcePaths} for more information).
     */
    public AbstractWebUiConfig(final String title, final Workflows workflow, final String[] externalResourcePaths) {
        this.title = title;
        this.webUiBuilder = new WebUiBuilder(this);
        this.desktopMainMenuConfig = new MainMenuBuilder(this);
        this.mobileMainMenuConfig = new MainMenuBuilder(this);

        this.workflow = workflow;

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
    public IMainMenuBuilder configDesktopMainMenu() {
        return desktopMainMenuConfig;
    }

    @Override
    public IMainMenuBuilder configMobileMainMenu() {
        return mobileMainMenuConfig;
    }

    @Override
    public final String genWebUiPreferences() {
        return webUiBuilder.genWebUiPrefComponent();
    }

    @Override
    public final String genDesktopMainWebUIComponent() {
        final Pair<DomElement, JsCode> generatedMenu = desktopMainMenuConfig.generateMenuActions();
        return ResourceLoader.getText("ua/com/fielden/platform/web/app/tg-desktop-app.html").
                replace("<!--menu action dom-->", generatedMenu.getKey().toString()).
                replace("//actionsObject", generatedMenu.getValue().toString());
    }

    @Override
    public final String genMobileMainWebUIComponent() {
        final Pair<DomElement, JsCode> generatedMenu = mobileMainMenuConfig.generateMenuActions();
        return ResourceLoader.getText("ua/com/fielden/platform/web/app/tg-mobile-app.html").
                replace("<!--menu action dom-->", generatedMenu.getKey().toString()).
                replace("//actionsObject", generatedMenu.getValue().toString());
    }

    @Override
    public final String genDesktopAppIndex() {
        final String indexSource = ResourceLoader.getText("ua/com/fielden/platform/web/desktop-index.html").
                replace("@title", title);
        if (isDevelopmentWorkflow(this.workflow)) {
            return indexSource.replace("@desktopStartupResources", "desktop-startup-resources-origin");
        } else {
            return indexSource.replace("@desktopStartupResources", "desktop-startup-resources-vulcanized");
        }
    }

    private static boolean isDevelopmentWorkflow(final Workflows workflow) {
        return Workflows.development.equals(workflow) || Workflows.vulcanizing.equals(workflow);
    }

    @Override
    public String genMobileAppIndex() {
        final String indexSource = ResourceLoader.getText("ua/com/fielden/platform/web/mobile-index.html").
                replace("@title", title);
        if (isDevelopmentWorkflow(this.workflow)) {
            return indexSource.replace("@mobileStartupResources", "mobile-startup-resources-origin");
        } else {
            return indexSource.replace("@mobileStartupResources", "mobile-startup-resources-vulcanized");
        }
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

    @Override
    public Map<String, AbstractCustomView> getCustomViews() {
        return webUiBuilder.getCustomViews();
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

    @Override
    public Workflows workflow() {
        return workflow;
    }

    @Override
    public final void clearConfiguration(final IGlobalDomainTreeManager gdtm) {
        logger.error("Clearing configurations...");
        this.webUiBuilder = new WebUiBuilder(this);
        this.desktopMainMenuConfig = new MainMenuBuilder(this);
        this.mobileMainMenuConfig = new MainMenuBuilder(this);
        logger.error("Clearing configurations...done");

        logger.error(String.format("Clearing centres for user [%s]...", gdtm.getUserProvider().getUser()));
        CentreUpdater.clearAllCentres(gdtm);
        logger.error(String.format("Clearing centres for user [%s]...done", gdtm.getUserProvider().getUser()));
    }

    @Override
    public Menu getMenuEntity() {
        return desktopMainMenuConfig.getMenu();
    }
}
