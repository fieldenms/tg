package ua.com.fielden.platform.client.ui.menu;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.swing.menu.ITreeMenuItemVisibilityProvider;
import ua.com.fielden.platform.swing.menu.TreeMenuItem;
import ua.com.fielden.platform.swing.menu.TreeMenuWithTabs;
import ua.com.fielden.platform.swing.menu.api.ITreeMenuFactory;
import ua.com.fielden.platform.swing.menu.api.ITreeMenuItemFactory;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfigController;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemInvisibilityController;
import ua.com.fielden.platform.ui.config.api.interaction.ICenterConfigurationController;
import ua.com.fielden.platform.ui.config.impl.interaction.RemoteCentreConfigurationController;

import com.google.inject.Injector;

/**
 * A factory for instantiation of the whole main menu tree.
 * 
 * @author TG Team
 * 
 */
public class TreeMenuFactory implements ITreeMenuFactory {

    private final Map<Class<? extends TreeMenuItem>, ITreeMenuItemFactory> bindings = new HashMap<Class<? extends TreeMenuItem>, ITreeMenuItemFactory>();
    private final TreeMenuItem root;
    private final TreeMenuWithTabs menu;
    private final Injector injector;
    private final ITreeMenuItemFactory defaultFactory;
    private final IEntityCentreConfigController eccController;
    private final IUserProvider userProvider;
    private final IMainMenuItemInvisibilityController mmiController;

    public TreeMenuFactory(final TreeMenuItem root, final TreeMenuWithTabs menu, final Injector injector) {
	this.root = root;
	this.menu = menu;
	this.injector = injector;
	this.defaultFactory = new TreeMenuItemFactory();
	this.eccController = injector.getInstance(IEntityCentreConfigController.class);
	this.userProvider = injector.getInstance(IUserProvider.class);
	this.mmiController = injector.getInstance(IMainMenuItemInvisibilityController.class);
    }

    @Override
    public ITreeMenuFactory bind(final Class<? extends TreeMenuItem> type, final ITreeMenuItemFactory itemFactory) {
	if (bindings.containsKey(type)) {
	    throw new IllegalStateException("Can only bind once");
	}
	bindings.put(type, itemFactory);
	return this;
    }

    @Override
    public void build(final List<MainMenuItem> itemsFromCloud) {
	for (final MainMenuItem rootItem : itemsFromCloud) {
	    traceTree(rootItem, root);
	}
    }

    private void traceTree(final MainMenuItem menuItem, final TreeMenuItem parent) {
	if (!menuItem.isPrincipal()) {
	    return;
	}
	final ITreeMenuItemFactory factory = getFactory(menuItem.getMenuItemType());
	final ICenterConfigurationController centerController = new RemoteCentreConfigurationController(eccController, menuItem, userProvider);
	final ITreeMenuItemVisibilityProvider visibilityProvider = new TreeMenuItemVisibilityProvider(menuItem, userProvider.getUser(), mmiController);
	final TreeMenuItem node = factory.create(menuItem.getMenuItemType(), menu, injector, centerController, visibilityProvider);
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
