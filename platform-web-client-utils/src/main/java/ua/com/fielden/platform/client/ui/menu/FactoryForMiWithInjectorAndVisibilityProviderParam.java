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

/**
 * A common factory for instantiating menu items represented by classes with a constructor having only one parameter -- injector.
 * 
 * @author TG Team
 * 
 */
public class FactoryForMiWithInjectorAndVisibilityProviderParam implements ITreeMenuItemFactory {

    private final Logger logger = Logger.getLogger(getClass());

    @Override
    public TreeMenuItem create(final Class menuItemClass, final TreeMenuWithTabs<?> treeMenu, final Injector injector, final ICenterConfigurationController centerController, final ITreeMenuItemVisibilityProvider visibilityProvider) {
	try {
	    final Constructor constructor = Reflector.getConstructorForClass(menuItemClass, Injector.class, ITreeMenuItemVisibilityProvider.class);
	    return (TreeMenuItem) constructor.newInstance(injector, visibilityProvider);
	} catch (final Exception ex) {
	    logger.error(ex);
	    throw new IllegalArgumentException("Most likely an incorrect menu item factory is being used.", ex);
	}
    }

}
