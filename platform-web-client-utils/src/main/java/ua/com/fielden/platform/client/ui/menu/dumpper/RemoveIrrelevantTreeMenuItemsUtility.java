package ua.com.fielden.platform.client.ui.menu.dumpper;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import ua.com.fielden.platform.algorithm.search.ITreeNode;
import ua.com.fielden.platform.algorithm.search.ITreeNodePredicate;
import ua.com.fielden.platform.algorithm.search.bfs.BreadthFirstSearch;
import ua.com.fielden.platform.swing.menu.TreeMenuItem;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemController;

/**
 * Traverses the remote main menu tree and for every node, which is not present in the local main menu tree removes it by deleting the remote instance.
 * <p>
 * This is one of the classes responsible for synchronising local main menu structure with remote (persisted) structure.
 *
 * @author TG Team
 *
 */
public class RemoveIrrelevantTreeMenuItemsUtility {

    /**
     * This method should be invoked to initiate main menu tree traversal to remove unnecessary main menu items.
     *
     * @param remoteMainMenuRoot
     *            -- the root element of the remote main menu to be checked for locally removed menu items.
     * @param controller
     *            -- main menu item controller, which can remove menu items.
     */
    public void cleanTreeMenu(final TreeMenuItem remoteMainMenuRoot, final TreeMenuItem localMainMenuRoot, final IMainMenuItemController controller) {
	new BreadthFirstSearch<ITreeNode, ITreeNode<ITreeNode>>().search(remoteMainMenuRoot, new TreeNodePredicate(controller, localMainMenuRoot));
    }

    /**
     * The implementation of {@link ITreeNodePredicate}, which performs the actual removal of individual main menu items.
     *
     * @author TG Team
     *
     */
    private static class TreeNodePredicate implements ITreeNodePredicate<ITreeNode, ITreeNode<ITreeNode>> {

	private final IMainMenuItemController controller;
	private final TreeMenuItem localMenuRoot;

	public TreeNodePredicate(final IMainMenuItemController controller, final TreeMenuItem localMenuRoot) {
	    this.controller = controller;
	    this.localMenuRoot = localMenuRoot;
	}

	/**
	 * Evaluates a menu item whether it should be persisted, and persists it if relevant.
	 */
	@Override
	public boolean eval(final ITreeNode<ITreeNode> node) {

	    final ITreeNode localNode = new BreadthFirstSearch<ITreeNode, ITreeNode<ITreeNode>>().search(localMenuRoot, new TreeNodeByClassPredicate(node));
	    if (localNode == null) {
		System.out.println("Need to remove " + node.getClass());
		final MainMenuItem menuItem = controller.findByKeyAndFetch(fetch(MainMenuItem.class).with("parent"), node.getClass().getName());
		if (menuItem != null) {
		    System.out.println("\tremoving " + menuItem);
		    controller.delete(menuItem);
		}
	    }

	    return false;
	}
    }

    /**
     * Matches visited nodes with the passed in node by class.
     */
    private static class TreeNodeByClassPredicate implements ITreeNodePredicate<ITreeNode, ITreeNode<ITreeNode>> {

	private final ITreeNode nodeToMatch;

	public TreeNodeByClassPredicate(final ITreeNode nodeToMatch) {
	    this.nodeToMatch = nodeToMatch;
	}

	@Override
	public boolean eval(final ITreeNode<ITreeNode> node) {
	    //System.out.println("Node to match " + nodeToMatch.getClass() + "with local " + node.getClass());
	    return node.getClass().equals(nodeToMatch.getClass());
	}

    }

}
