package ua.com.fielden.platform.client.ui.menu;

import java.lang.reflect.Constructor;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.swing.menu.ITreeMenuItemVisibilityProvider;
import ua.com.fielden.platform.swing.menu.MiGroupItem;
import ua.com.fielden.platform.swing.menu.TreeMenuItem;
import ua.com.fielden.platform.swing.menu.TreeMenuWithTabs;
import ua.com.fielden.platform.swing.menu.api.ITreeMenuItemFactory;
import ua.com.fielden.platform.ui.config.api.interaction.ICenterConfigurationController;

import com.google.inject.Injector;

/**
 * The default implementation of the {@link ITreeMenuItemFactory}, used in case if there is no custom implementation associated with a particular main menu class.
 * <p>
 * This implementation assumes that non-group menu item type has a constructor with four parameters ({@link TreeMenuWithTabs}, {@link Injector}, {@link ICenterConfigurationController}, {@link ITreeMenuItemVisibilityProvider})
 * and the group menu items (i.e. derived from {@link MiGroupItem}) have the constructor with a single parameter of type {@link ITreeMenuItemVisibilityProvider}.
 *
 * @author TG Team
 *
 */
public class DefaultTreeMenuItemFactory implements ITreeMenuItemFactory {

    private final Logger logger = Logger.getLogger(getClass());

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public TreeMenuItem create(final Class menuItemClass, final TreeMenuWithTabs<?> treeMenu, final Injector injector, final ICenterConfigurationController centerController, final ITreeMenuItemVisibilityProvider visibilityProvider) {
	// try creating an item based on a constructor for non-group items
	// if it fails then try group item option
	try {
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
