package ua.com.fielden.platform.client.ui.menu;

import java.lang.reflect.Constructor;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.swing.menu.ITreeMenuItemVisibilityProvider;
import ua.com.fielden.platform.swing.menu.TreeMenuItem;
import ua.com.fielden.platform.swing.menu.TreeMenuWithTabs;
import ua.com.fielden.platform.swing.menu.api.ITreeMenuItemFactory;
import ua.com.fielden.platform.ui.config.api.interaction.ICenterConfigurationController;

import com.google.inject.Injector;

public class TreeMenuItemFactory implements ITreeMenuItemFactory {

    private final Logger logger = Logger.getLogger(getClass());

    /**
     * An implementation assumes that non-group "menuItemClass" has a "three-parametered" constructor and group menu item - default constructor.
     */
    @Override
    public TreeMenuItem create(final Class menuItemClass, final TreeMenuWithTabs<?> treeMenu, final Injector injector, final ICenterConfigurationController centerController, final ITreeMenuItemVisibilityProvider visibilityProvider) {
	try { // find three parametered constructor with parameters : [final TreeMenuWithTabs<?> treeMenu, final Injector injector, final ICenterConfigurationController centerController]
	    final Constructor constructor = Reflector.getConstructorForClass(menuItemClass, TreeMenuWithTabs.class, Injector.class, ICenterConfigurationController.class, ITreeMenuItemVisibilityProvider.class);
	    return (TreeMenuItem) constructor.newInstance(treeMenu, injector, centerController, visibilityProvider);
	} catch (final NoSuchMethodException e) {
	    try { // find default constructor:
		final Constructor constructor = Reflector.getConstructorForClass(menuItemClass, ITreeMenuItemVisibilityProvider.class);
		return (TreeMenuItem) constructor.newInstance(visibilityProvider);
	    } catch (final Exception ex) {
		logger.error(ex);
		throw new IllegalArgumentException("Most likely this is an incorrect factory for this type of menu item.", ex);
	    }
	} catch (final Exception ex) {
	    logger.error(ex);
	    throw new IllegalArgumentException("Most likely this is an incorrect factory for this type of menu item.", ex);
	}
    }
}
