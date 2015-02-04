package ua.com.fielden.platform.web.app.config;

import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.app.IWebApp;

/**
 * Implementation of the {@link IWebAppConfig}.
 *
 * @author TG Team
 *
 */
public class WebAppConfig implements IWebAppConfig {

    /**
     * The {@link IWebApp} instance for which this configuration object was created.
     */
    private final IWebApp webApplication;

    private int minDesktopWidth = 980, minTabletWidth = 768;
    private String locale = "en-AU";

    /**
     * Creates new instance of {@link WebAppConfig} for the specified {@link IWebApp} instance.
     *
     * @param webApplication
     */
    public WebAppConfig(final IWebApp webApplication) {
        this.webApplication = webApplication;
    }

    @Override
    public IWebAppConfig setMinDesktopWidth(final int width) {
        this.minDesktopWidth = width;
        return this;
    }

    @Override
    public IWebAppConfig setMinTabletWidth(final int width) {
        this.minTabletWidth = width;
        return this;
    }

    @Override
    public IWebAppConfig setLocale(final String locale) {
        this.locale = locale;
        return this;
    }

    @Override
    public IWebApp end() {
        return webApplication;
    }

    /**
     * Generates the html representation of the web application configuration object
     *
     * @return
     */
    public String generateConfigComponent() {
        if (this.minDesktopWidth <= this.minTabletWidth) {
            throw new IllegalStateException("The desktop width can not be less then or equal tablet width.");
        }
        return ResourceLoader.getText("ua/com/fielden/platform/web/app/config/tg-app-config.html").
                replaceAll("@minDesktopWidth", Integer.toString(this.minDesktopWidth)).
                replaceAll("@minTabletWidth", Integer.toString(this.minTabletWidth)).
                replaceAll("@locale", "\"" + locale + "\"");
    }

}
