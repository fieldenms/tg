package ua.com.fielden.platform.client.ui.menu;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.swing.menu.ITreeMenuItemVisibilityProvider;
import ua.com.fielden.platform.swing.menu.MiWithVisibilityProvider;
import ua.com.fielden.platform.swing.menu.TreeMenuItem;
import ua.com.fielden.platform.swing.menu.TreeMenuWithTabs;
import ua.com.fielden.platform.swing.menu.api.ITreeMenuFactory;
import ua.com.fielden.platform.swing.menu.api.ITreeMenuItemFactory;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemInvisibilityController;

import com.google.inject.Injector;

/**
 * A factory for instantiation of the whole main menu tree.
 *
 * @author TG Team
 *
 */
public class TreeMenuFactory implements ITreeMenuFactory {
    private final Map<Class<?>, ITreeMenuItemFactory> bindings = new HashMap<Class<?>, ITreeMenuItemFactory>();
    private final TreeMenuItem<?> root;
    private final TreeMenuWithTabs<?> menu;
    private final Injector injector;
    private final ITreeMenuItemFactory defaultFactory;
    private final IUserProvider userProvider;
    private final IMainMenuItemInvisibilityController mmiController;

    public TreeMenuFactory(final TreeMenuItem<?> root, final TreeMenuWithTabs<?> menu, final Injector injector) {
	this.root = root;
	this.menu = menu;
	this.injector = injector;
	this.defaultFactory = new DefaultTreeMenuItemFactory();
	this.userProvider = injector.getInstance(IUserProvider.class);
	this.mmiController = injector.getInstance(IMainMenuItemInvisibilityController.class);
    }

    @Override
    public void build(final List<MainMenuItem> mainMenuItems) {
	for (final MainMenuItem rootItem : mainMenuItems) {
	    traceTree(rootItem, root);
	}
    }

    @Override
    public ITreeMenuFactory bind(final Class<? extends TreeMenuItem> type, final ITreeMenuItemFactory itemFactory) {
	if (bindings.containsKey(type)) {
	    throw new IllegalStateException("Can only bind once");
	}
	bindings.put(type, itemFactory);
	return this;
    }

    private void traceTree(final MainMenuItem menuItem, final TreeMenuItem<?> parent) {
	if (!menuItem.isPrincipal()) {
	    return;
	}
	final ITreeMenuItemFactory factory = getFactory(menuItem.getMenuItemType());
	final ITreeMenuItemVisibilityProvider visibilityProvider = new TreeMenuItemVisibilityProvider(menuItem, userProvider.getUser(), mmiController);
	final MiWithVisibilityProvider<?> node = factory.create(menuItem.getMenuItemType(), menu, injector, visibilityProvider);
	parent.addItem(node);
	for (final MainMenuItem child : menuItem.getChildren()) {
	    traceTree(child, node);
	}
    }

    private ITreeMenuItemFactory getFactory(final Class<?> menuItemType) {
	final ITreeMenuItemFactory itemFactory = bindings.get(menuItemType);
	if (itemFactory == null) {
	    return defaultFactory;
	}
	return itemFactory;
    }
}
