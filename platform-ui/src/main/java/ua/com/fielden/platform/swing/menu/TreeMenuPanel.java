package ua.com.fielden.platform.swing.menu;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.border.LineBorder;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.swing.filteredtree.FilterableTreePanel;
import ua.com.fielden.platform.swing.menu.filter.ui.FilterControl;

/**
 * A control conveniently incorporating {@link TreeMenu} and the search bar.
 * 
 * @author 01es
 * 
 */
public class TreeMenuPanel extends FilterableTreePanel {
    private static final long serialVersionUID = 1L;

    public TreeMenuPanel(final TreeMenu<?> treeMenu) {
        this(treeMenu, 150);
    }

    public TreeMenuPanel(final TreeMenu<?> treeMenu, final int minWidth) {
        super(treeMenu, "find menu...", minWidth);

        // //////////////////////////////
        // assign navigation hot keys //
        // //////////////////////////////
        // create CTRL+1 key stroke and a corresponding action to bring focus to the tree menu
        final KeyStroke activateTreeMenu = KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.CTRL_DOWN_MASK);
        final String ACTIVATE_TREE_MENU = "ACTIVATE_TREE_MENU";
        treeMenu.getActionMap().put(ACTIVATE_TREE_MENU, createFocusTreeMenuAction(treeMenu));
        treeMenu.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(activateTreeMenu, ACTIVATE_TREE_MENU);
        // create CTRL+2 key stroke and a corresponding action to bring focus to the filter control
        final KeyStroke activateFilterControl = KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.CTRL_DOWN_MASK);
        final String ACTIVATE_FILTER_CONTROL = "ACTIVATE_FILTER_CONTROL";
        getFilterControl().getActionMap().put(ACTIVATE_FILTER_CONTROL, createFocusFilterControlAction(getFilterControl()));
        getFilterControl().getInputMap(WHEN_IN_FOCUSED_WINDOW).put(activateFilterControl, ACTIVATE_FILTER_CONTROL);
        // create CTRL+3 key stroke and a corresponding action to bring focus to the holder panel
        final KeyStroke focusHolder = KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.CTRL_DOWN_MASK);
        final String FOCUS_HOLDER = "FOCUS_HOLDER";
        getFilterControl().getActionMap().put(FOCUS_HOLDER, createFocusHolderAction(treeMenu));
        getFilterControl().getInputMap(WHEN_IN_FOCUSED_WINDOW).put(focusHolder, FOCUS_HOLDER);
        treeMenu.expandAll();
    }

    /**
     * Creates an action to focus the holder panel.
     * 
     * @param treeMenu
     * @return
     */
    private Action createFocusHolderAction(final TreeMenu<?> treeMenu) {
        return new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(final ActionEvent e) {
                treeMenu.getHolder().requestFocusInWindow();
            }
        };
    }

    private Action createFocusFilterControlAction(final FilterControl filterControl) {
        return new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(final ActionEvent e) {
                filterControl.requestFocusInWindow();
            }
        };
    }

    /**
     * Creates an focus to active tree menu
     * 
     * @param treeMenu
     * @return
     */
    private Action createFocusTreeMenuAction(final TreeMenu<?> treeMenu) {
        return new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(final ActionEvent e) {
                treeMenu.requestFocusInWindow();
            }
        };
    }

    /**
     * This is a convenient factory method for creation of a panel with predefined layout and border to be used as a tree menu item content holder.
     * 
     * @return
     */
    public static JPanel createContentHolder() {
        final JPanel holder = new JPanel(new MigLayout("fill, insets 0", "[grow,fill]", "[grow,fill]"));
        holder.setBorder(new LineBorder(new Color(140, 140, 140)));
        return holder;
    }

    @Override
    public TreeMenu<?> getTree() {
        return (TreeMenu<?>) super.getTree();
    }

}
