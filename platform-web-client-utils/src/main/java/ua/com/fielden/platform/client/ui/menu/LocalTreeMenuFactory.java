package ua.com.fielden.platform.client.ui.menu;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.swing.menu.ITreeMenuItemVisibilityProvider;
import ua.com.fielden.platform.swing.menu.TreeMenuItem;
import ua.com.fielden.platform.swing.menu.TreeMenuWithTabs;
import ua.com.fielden.platform.swing.menu.api.ITreeMenuFactory;
import ua.com.fielden.platform.swing.menu.api.ITreeMenuItemFactory;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.api.interaction.ICenterConfigurationController;

import com.google.inject.Injector;

/**
 * A factory for instantiation of the whole main menu tree from the local rather than remote resource. It exists to support the developer workflow.
 *
 * @author TG Team
 *
 */
public class LocalTreeMenuFactory implements ITreeMenuFactory {

    private final Map<Class<? extends TreeMenuItem>, ITreeMenuItemFactory> bindings = new HashMap<Class<? extends TreeMenuItem>, ITreeMenuItemFactory>();
    private final TreeMenuItem root;
    private final TreeMenuWithTabs menu;
    private final Injector injector;
    private final ITreeMenuItemFactory defaultFactory;

    public LocalTreeMenuFactory(final TreeMenuItem root, final TreeMenuWithTabs menu, final Injector injector) {
	this.root = root;
	this.menu = menu;
	this.injector = injector;
	this.defaultFactory = new DefaultTreeMenuItemFactory();
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
	final ICenterConfigurationController centerController = injector.getInstance(ICenterConfigurationController.class);
	final ITreeMenuItemVisibilityProvider visibilityProvider = injector.getInstance(ITreeMenuItemVisibilityProvider.class);;
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
