package ua.com.fielden.platform.client.ui.menu.dumpper;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import ua.com.fielden.platform.algorithm.search.ITreeNode;
import ua.com.fielden.platform.algorithm.search.ITreeNodePredicate;
import ua.com.fielden.platform.algorithm.search.bfs.BreadthFirstSearch;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.swing.menu.MiSaveAsConfiguration;
import ua.com.fielden.platform.swing.menu.TreeMenuItem;
import ua.com.fielden.platform.swing.menu.TreeMenuItemWrapper;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemController;

/**
 * Traverses the main menu tree and persists each relevant item together with its configuration stored locally.
 * <p>
 * This is one of the classes responsible for synchronising local main menu structure with remote (persisted) structure.
 * 
 * @author TG Team
 * 
 */
public class AdjustTreeMenuStructureUtility {

    /**
     * This method should be invoked to initiate main menu tree traversal to persist main menu items.
     * 
     * @param menuRoot
     *            -- the root element of the main menu to be persisted.
     * @param controller
     *            -- main menu item controller, which can persist a menu item.
     * @param factory
     *            -- entity factory required for creation of menu instance.
     */
    public void saveTreeMenu(final TreeMenuItem menuRoot, final IMainMenuItemController controller, final EntityFactory factory) {
        new BreadthFirstSearch<ITreeNode, ITreeNode<ITreeNode>>().search(menuRoot, new TreeNodePredicate(controller, factory));
    }

    /**
     * The implementation of {@link ITreeNodePredicate}, which performs the actual creation/update of an individual main menu item.
     * 
     * @author TG Team
     * 
     */
    private static class TreeNodePredicate implements ITreeNodePredicate<ITreeNode, ITreeNode<ITreeNode>> {

        private final IMainMenuItemController controller;
        private final EntityFactory factory;

        private int order = 0;

        public TreeNodePredicate(final IMainMenuItemController controller, final EntityFactory factory) {
            this.controller = controller;
            this.factory = factory;
        }

        /**
         * Evaluates a menu item whether it should be persisted, and persists it if relevant.
         */
        @Override
        public boolean eval(final ITreeNode<ITreeNode> node) {
            if (canSave(node)) {
                MainMenuItem menuItem = controller.findByKeyAndFetch(fetch(MainMenuItem.class).with("parent"), node.getClass().getName());
                if (menuItem == null) {
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
            return node.state() != null && !node.getClass().equals(MiSaveAsConfiguration.class) && !node.getClass().equals(TreeMenuItemWrapper.class);
        }

    }

}
