package ua.com.fielden.platform.swing.menu.api;

import ua.com.fielden.platform.swing.menu.ITreeMenuItemVisibilityProvider;
import ua.com.fielden.platform.swing.menu.MiWithVisibilityProvider;
import ua.com.fielden.platform.swing.menu.TreeMenuItem;
import ua.com.fielden.platform.swing.menu.TreeMenuWithTabs;

import com.google.inject.Injector;

/**
 * A factory construct for {@link TreeMenuItem}s creation from item class and the appropriate configuration controller.
 * 
 * @author TG Team
 * 
 */
public interface ITreeMenuItemFactory {

    /**
     * Creates a tree menu item from its class and appropriate configuration controller.
     * 
     * @param clazz
     *            -- a type for which menu item must be created.
     * @param treeMenu
     *            -- a tree menu to which an item will be added
     * @param injector
     *            -- for getting all other needed information
     * @param visibilityProvider
     *            -- a contract to identify menu item's visibility
     * @return
     */
    MiWithVisibilityProvider<?> create(final Class<?> clazz, final TreeMenuWithTabs<?> treeMenu, final Injector injector, final ITreeMenuItemVisibilityProvider visibilityProvider);

}
