package ua.com.fielden.platform.web.menu;


import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;

import java.util.stream.Stream;

/**
 * An API entry point for building application's main menu.
 *
 * @author TG Team
 *
 */
public interface IMainMenuBuilder {

    /**
     * Adds new module to main menu, meaning that each main menu item is associated with a logical application module.
     *
     * @param title
     * @return
     */
    IModuleConfig addModule(String title);

    /// Creates a stream of all action configurations in this main menu.
    ///
    Stream<EntityActionConfig> streamActionConfigs();

}
