package ua.com.fielden.platform.web.app.config;

import ua.com.fielden.platform.web.app.IWebApp;

/**
 * Web application's global configuration object.
 *
 * @author TG Team
 *
 */
public interface IWebAppConfig {

    /**
     * Set the minimal desktop width.
     *
     * @param width
     * @return
     */
    IWebAppConfig setMinDesktopWidth(int width);

    /**
     * Set the minimal tablet width
     *
     * @param width
     * @return
     */
    IWebAppConfig setMinTabletWidth(int width);

    /**
     * Set the locale for the web application.
     *
     * @param locale
     * @return
     */
    IWebAppConfig setLocale(String locale);

    /**
     * Finish to configure the web application.
     *
     * @return
     */
    IWebApp end();

}
