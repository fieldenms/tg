package ua.com.fielden.platform.client.ui.menu.dumpper;

import java.util.Enumeration;

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.swing.menu.MiSaveAsConfiguration;
import ua.com.fielden.platform.swing.menu.TreeMenuItem;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemController;

public class TreeMenuDumpUtility {

    public void saveTreeMenu(final TreeMenuItem menuItem, final IMainMenuItemController controller, final EntityFactory factory){
	traceTree(new TreePath(menuItem), null, 0, new TreeNodeVisitor(controller, factory));
    }

    private static void traceTree(final TreePath treePath, final MainMenuItem parent, final int order, final ITreeNodeVisitor visitor) {
	final Object lastPathConponent = treePath.getLastPathComponent();
	if (lastPathConponent instanceof MiSaveAsConfiguration) {
	    return;
	}
	final TreeMenuItem node = (TreeMenuItem) treePath.getLastPathComponent();
	MainMenuItem newParent = parent;
	if (!node.isRoot()) {
	    newParent = visitor.visit(node, newParent, order);
	}
	if (node.getChildCount() > 0) {
	    int childOrder = 1;
	    for (final Enumeration<?> childrenEnum = node.children(); childrenEnum.hasMoreElements();) {
		final TreeNode n = (TreeNode) childrenEnum.nextElement();
		final TreePath path = treePath.pathByAddingChild(n);
		traceTree(path, newParent, childOrder++, visitor);
	    }
	}
    }

    private static class TreeNodeVisitor implements ITreeNodeVisitor {

	private final IMainMenuItemController controller;
	private final EntityFactory factory;

	public TreeNodeVisitor(final IMainMenuItemController controller, final EntityFactory factory) {
	    this.controller = controller;
	    this.factory = factory;
	}

	@Override
	public MainMenuItem visit(final TreeMenuItem item, final MainMenuItem parent, final int order) {
	    final MainMenuItem menuItem = factory.newByKey(MainMenuItem.class, item.getClass().getName());
	    menuItem.setParent(parent);
	    menuItem.setTitle(menuItem.getKey());
	    menuItem.setOrder(order);
	    return controller.save(menuItem);
	}

    }

}
