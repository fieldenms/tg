package ua.com.fielden.platform.web.app;

import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.app.config.IWebAppConfig;
import ua.com.fielden.platform.web.app.config.WebAppConfig;
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
                replaceAll("@title", title);
    }
}
