package ua.com.fielden.platform.web.menu;

import ua.com.fielden.platform.web.app.IWebApp;

/**
 * The contract for anything that should be layout.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IMenuLayoutConfigWithDone extends IMenuLayoutConfig {

    IWebApp done();
}
