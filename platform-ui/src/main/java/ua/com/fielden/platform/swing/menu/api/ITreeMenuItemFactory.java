package ua.com.fielden.platform.swing.menu.api;

import ua.com.fielden.platform.swing.menu.ITreeMenuItemVisibilityProvider;
import ua.com.fielden.platform.swing.menu.TreeMenuItem;
import ua.com.fielden.platform.swing.menu.TreeMenuWithTabs;
import ua.com.fielden.platform.ui.config.api.interaction.ICenterConfigurationController;

import com.google.inject.Injector;

/**
 * A factory for {@link TreeMenuItem}s creation from item class and appropriate configuration controller.
 * 
 * @author TG Team
 * 
 */
public interface ITreeMenuItemFactory {

    /**
     * Creates a tree menu item from its class and appropriate configuration controller.
     * 
     * @param menuItemClass
     *            - a type of tree menu item
     * @param treeMenu
     *            - a tree menu to which an item will be added
     * @param injector
     *            - for getting all other needed information
     * @param centerController
     *            - an {@link ICenterConfigurationController} for new menu item
     * @param visibilityProvider TODO
     * @return
     */
    TreeMenuItem<?> create(final Class menuItemClass, final TreeMenuWithTabs<?> treeMenu, final Injector injector, final ICenterConfigurationController centerController, ITreeMenuItemVisibilityProvider visibilityProvider);

}
