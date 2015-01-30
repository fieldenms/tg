package ua.com.fielden.platform.web.app.config;

import ua.com.fielden.platform.web.app.IWebApp;

public interface IWebAppConfig {

    IWebAppConfig setMinDesktopWidth(int width);

    IWebAppConfig setMinTabletWidth(int width);

    IWebApp end();

}
