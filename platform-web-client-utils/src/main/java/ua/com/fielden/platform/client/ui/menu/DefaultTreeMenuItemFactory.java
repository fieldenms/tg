package ua.com.fielden.platform.client.ui.menu;

import java.lang.reflect.Constructor;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.swing.menu.ITreeMenuItemVisibilityProvider;
import ua.com.fielden.platform.swing.menu.MiGroupItem;
import ua.com.fielden.platform.swing.menu.MiWithVisibilityProvider;
import ua.com.fielden.platform.swing.menu.TreeMenuWithTabs;
import ua.com.fielden.platform.swing.menu.api.ITreeMenuItemFactory;

import com.google.inject.Injector;

/**
 * The default implementation of the {@link ITreeMenuItemFactory}, used in case if there is no custom implementation associated with a particular main menu class.
 * <p>
 * This implementation assumes that non-group menu item type has a constructor with four parameters ({@link TreeMenuWithTabs}, {@link Injector}, {@link ITreeMenuItemVisibilityProvider})
 * and the group menu items (i.e. derived from {@link MiGroupItem}) have the constructor with a single parameter of type {@link ITreeMenuItemVisibilityProvider}.
 *
 * @author TG Team
 *
 */
public class DefaultTreeMenuItemFactory implements ITreeMenuItemFactory {

    private final Logger logger = Logger.getLogger(getClass());

    @Override
    public MiWithVisibilityProvider<?> create(final Class<?> clazz, final TreeMenuWithTabs<?> treeMenu, final Injector injector, final ITreeMenuItemVisibilityProvider visibilityProvider) {
	// try creating an item based on a constructor for non-group items
	// if it fails then try group item option
	try {
	    final Constructor<?> constructor = Reflector.getConstructorForClass(clazz, TreeMenuWithTabs.class, Injector.class, ITreeMenuItemVisibilityProvider.class);
	    try {
	        return (MiWithVisibilityProvider<?>) constructor.newInstance(treeMenu, injector, visibilityProvider);
	    } catch (final Exception ex){
		ex.printStackTrace();
	    }
	} catch(final Exception ex){
	    logger.debug(ex);
	}
	try {
	    final Constructor<?> constructor = Reflector.getConstructorForClass(clazz, Injector.class, ITreeMenuItemVisibilityProvider.class);
	    return (MiWithVisibilityProvider<?>) constructor.newInstance(injector, visibilityProvider);
	}catch (final Exception ex) {
	    logger.debug(ex);
	}
	try { // find default constructor:
	    final Constructor<?> constructor = Reflector.getConstructorForClass(clazz, ITreeMenuItemVisibilityProvider.class);
	    return (MiWithVisibilityProvider<?>) constructor.newInstance(visibilityProvider);
	} catch (final Exception ex) {
	    logger.error(ex);
	    throw new IllegalArgumentException("Most likely this is an incorrect factory for type [" + clazz.getSimpleName() + "] of menu item.", ex);
	}
    }
}
