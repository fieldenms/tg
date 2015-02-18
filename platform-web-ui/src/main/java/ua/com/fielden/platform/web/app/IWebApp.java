package ua.com.fielden.platform.web.app;

import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.app.config.IWebAppConfig;
import ua.com.fielden.platform.web.menu.IMainMenuConfig;
import ua.com.fielden.platform.web.view.master.EntityMaster;

/**
 * API for web application configuring.
 *
 * @author TG Team
 *
 */
public interface IWebApp {

    /**
     * Provides access to the global application configuration object.
     *
     * @return
     */
    IWebAppConfig configApp();

    /**
     * Provides access to the application's main menu configuration object.
     *
     * @return
     */
    IMainMenuConfig configMainMenu();

    /**
     * TODO Temporal method for registering masters. Please remove and provide fluent interface.
     *
     * @param entityMaster
     * @return
     */
    <T extends AbstractEntity<?>> IWebApp addMaster(final EntityMaster<T> entityMaster);

    /**
     * TODO Temporal method for getting masters. Please remove and provide fluent interface.
     *
     * @return
     */
    List<EntityMaster<? extends AbstractEntity<?>>> getMasters();
}
