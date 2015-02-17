package ua.com.fielden.platform.web.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.app.config.IWebAppConfig;
import ua.com.fielden.platform.web.app.config.WebAppConfig;
import ua.com.fielden.platform.web.master.EntityMaster;
import ua.com.fielden.platform.web.menu.IMainMenuConfig;
import ua.com.fielden.platform.web.menu.MainMenuConfig;

/**
 * The implementation for web application.
 *
 * @author TG Team
 *
 */
public class WebApp implements IWebApp {

    private final String title;
    private final WebAppConfig webAppConfig;
    private final MainMenuConfig mainMenuConfig;
    private final List<EntityMaster<? extends AbstractEntity<?>>> masters = new ArrayList<>(); // TODO temporal

    public WebApp(final String title) {
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
    public String generateGlobalConfig() {
        return webAppConfig.generateConfigComponent();
    }

    /**
     * Generates the main menu component.
     *
     * @return
     */
    public String generateMainMenu() {
        return mainMenuConfig.generateMainMenu();
    }

    /**
     * Generates the web application.
     *
     * @return
     */
    public String generateWebApp() {
        return ResourceLoader.getText("ua/com/fielden/platform/web/app/tg-web-app.html").
                replaceAll("@title", title).
                replaceAll("@views", mainMenuConfig.generateMenuViews());
    }

    /**
     * Returns the map of entity masters for this web application.
     *
     * @return
     */
    public Map<String, String> getMasterMap() {
        return webAppConfig.getMasters();
    }

    @Override
    public <T extends AbstractEntity<?>> IWebApp addMaster(final EntityMaster<T> entityMaster) {
        masters.add(entityMaster);
        return this;
    }

    /**
     * Returns registered entity masters.
     *
     * @return
     */
    public List<EntityMaster<? extends AbstractEntity<?>>> getMasters() {
        return Collections.unmodifiableList(masters);
    }
}
