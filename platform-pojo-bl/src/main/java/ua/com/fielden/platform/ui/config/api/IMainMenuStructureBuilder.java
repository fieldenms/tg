package ua.com.fielden.platform.ui.config.api;

import java.util.List;

import ua.com.fielden.platform.ui.config.MainMenuItem;

/**
 * A contract for building a hierarchical main menu structure.
 *
 * @author TG Team
 *
 */
public interface IMainMenuStructureBuilder {
    /**
     * Should return a hierarchical structure of the application main menu, where the returned list contains the first level menu items (i.e. the children of the root node).
     *
     * @return
     */
    List<MainMenuItem> build();
}
