package ua.com.fielden.platform.web.app;

import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.app.config.IWebAppConfig;
import ua.com.fielden.platform.web.app.config.WebAppConfig;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.menu.IMainMenuConfig;
import ua.com.fielden.platform.web.menu.MainMenuConfig;
import ua.com.fielden.platform.web.view.master.EntityMaster;

/**
 * The implementation for web application.
 *
 * @author TG Team
 *
 */
public abstract class AbstractWebApp implements IWebApp {

    private final String title;
    private final WebAppConfig webAppConfig;
    private final MainMenuConfig mainMenuConfig;

    public AbstractWebApp(final String title) {
        this.title = title;
        this.webAppConfig = new WebAppConfig(this);
        this.mainMenuConfig = new MainMenuConfig(this);
    }

    @Override
    public IWebAppConfig configApp() {
        return webAppConfig;
    }

    @Override
    public IMainMenuConfig configMainMenu() {
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
        return mainMenuConfig.generateMainMenu();
    }

    /**
     * Generates the web application.
     *
     * @return
     */
    @Override
    public final String generateWebApp() {
        return ResourceLoader.getText("ua/com/fielden/platform/web/app/tg-web-app.html").
                replaceAll("@title", title).
                replaceAll("@views", mainMenuConfig.generateMenuViews());
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
    public final Map<Class<? extends MiWithConfigurationSupport<?>>, EntityCentre> getCentres() {
        return webAppConfig.getCentres();
    }
}
