package ua.com.fielden.platform.web.app;

import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.app.config.IWebUiBuilder;
import ua.com.fielden.platform.web.app.config.WebUiBuilder;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.menu.IMainMenuBuilder;
import ua.com.fielden.platform.web.menu.impl.MainMenuBuilder;
import ua.com.fielden.platform.web.view.master.EntityMaster;

import com.google.inject.Injector;

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
    private final WebUiBuilder webAppConfig;
    private final MainMenuBuilder mainMenuConfig;
    private Injector injector;

    public AbstractWebUiConfig(final String title) {
        this.title = title;
        this.webAppConfig = new WebUiBuilder(this);
        this.mainMenuConfig = new MainMenuBuilder(this);
    }

    @Override
    public IWebUiBuilder configApp() {
        return webAppConfig;
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
    public final String generateGlobalConfig() {
        return webAppConfig.generateConfigComponent();
    }

    /**
     * Generates the main menu component.
     *
     * @return
     */
    @Override
    public final String generateMainMenu() {
        return ResourceLoader.getText("ua/com/fielden/platform/web/app/tg-app.html").
                replaceAll("@menuConfig", mainMenuConfig.code().toString());
    }

    /**
     * Generates the web application.
     *
     * @return
     */
    @Override
    public final String generateWebApp() {
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
        return webAppConfig.getMasters();
    }

    /**
     * Returns the map of entity centres for this web application.
     *
     * @return
     */
    @Override
    public final Map<Class<? extends MiWithConfigurationSupport<?>>, EntityCentre<?>> getCentres() {
        return webAppConfig.getCentres();
    }

    public void setInjector(final Injector injector) {
        this.injector = injector;
    }

    protected Injector injector() {
        return injector;
    }
}
