package ua.com.fielden.platform.client.ui.menu.dumpper;

import ua.com.fielden.platform.algorithm.search.ITreeNode;
import ua.com.fielden.platform.algorithm.search.ITreeNodePredicate;
import ua.com.fielden.platform.algorithm.search.bfs.BreadthFirstSearch;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.equery.fetch;
import ua.com.fielden.platform.swing.menu.MiSaveAsConfiguration;
import ua.com.fielden.platform.swing.menu.TreeMenuItem;
import ua.com.fielden.platform.swing.menu.TreeMenuItemWrapper;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemController;

public class TreeMenuDumpUtility {

    public void saveTreeMenu(final TreeMenuItem menuItem, final IMainMenuItemController controller, final EntityFactory factory){
	new BreadthFirstSearch<ITreeNode, ITreeNode<ITreeNode>>().search(menuItem, new TreeNodePredicate(controller, factory));
    }


    private static class TreeNodePredicate implements ITreeNodePredicate<ITreeNode, ITreeNode<ITreeNode>>{

	private final IMainMenuItemController controller;
	private final EntityFactory factory;

	private int order = 0;

	public TreeNodePredicate(final IMainMenuItemController controller, final EntityFactory factory) {
	    this.controller = controller;
	    this.factory = factory;
	}


	@Override
	public boolean eval(final ITreeNode<ITreeNode> node) {
	    if(canSave(node)){
		MainMenuItem menuItem = controller.findByKeyAndFetch(new fetch<MainMenuItem>(MainMenuItem.class).with("parent"), node.getClass().getName());
		if(menuItem == null){
		    menuItem = factory.newByKey(MainMenuItem.class, node.getClass().getName());
		}
		final MainMenuItem parent = controller.findByKey(node.state().getClass().getName());
		menuItem.setParent(parent);
		menuItem.setTitle(menuItem.getKey());
		menuItem.setOrder(order);
		controller.save(menuItem);
		order++;
	    }
	    return false;
	}


	private boolean canSave(final ITreeNode<ITreeNode> node) {
	    return node.state() !=null && !node.getClass().equals(MiSaveAsConfiguration.class) && !node.getClass().equals(TreeMenuItemWrapper.class);
	}

    }

}
