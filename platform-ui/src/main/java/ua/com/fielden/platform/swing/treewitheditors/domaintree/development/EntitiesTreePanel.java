package ua.com.fielden.platform.swing.treewitheditors.domaintree.development;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreeNode;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.domaintree.IDomainTreeManager;
import ua.com.fielden.platform.swing.menu.filter.IFilterableModel;
import ua.com.fielden.platform.swing.menu.filter.ui.FilterControl;
import ua.com.fielden.platform.swing.treetable.ViewportChangeEvent;

public class EntitiesTreePanel<DTM extends IDomainTreeManager> extends JPanel {

    private static final long serialVersionUID = 8553947680672851151L;

    private final EntitiesTree2<DTM> entitiesTree;

    //    private final JPanel search;
    //
    //    private final FilterControl filterControl;

    public EntitiesTreePanel(final EntitiesTree2<DTM> entitiesTree) {
        super(new MigLayout("fill, insets 0", "[150::,grow,fill]", "[:30:][grow,fill]"));

        this.entitiesTree = entitiesTree;

        setBorder(new LineBorder(new Color(140, 140, 140)));
        setBackground(Color.white);

        // ///////////////////////////////////////////////////
        // create the search panel with filter control and //
        // ///////////////////////////////////////////////////
        final JPanel search = new JPanel(new MigLayout("fill, insets 2 2 2 2", "[fill,grow]1[]1[]1[]", "[fill,grow]"));
        search.setBackground(Color.white);
        final IFilterableModel filterableModel = getTree().getEntitiesModel().getFilterableModel();
        final JTextField filterTextControl = new JTextField();
        final FilterControl filterControl = new FilterControl(filterTextControl, filterableModel, "find property...");
        search.add(filterControl, "grow");
        // create ENTER key stroke and a corresponding action for selecting the first matched menu item in the tree
        final KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0); // no modifiers
        final String SELECT_MENU_ITEM = "SELECT_MENU_ITEM";
        filterTextControl.getActionMap().put(SELECT_MENU_ITEM, createSelectItemAction(getTree(), filterableModel));
        filterTextControl.getInputMap().put(enter, SELECT_MENU_ITEM);
        // create ESC key stroke for clearing filter
        final KeyStroke esc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0); // no modifiers
        final String CLEAR_FILTER = "CLEAR_FILTER";
        filterTextControl.getActionMap().put(CLEAR_FILTER, createClearFilterAction(filterControl));
        filterTextControl.getInputMap().put(esc, CLEAR_FILTER);

        // /////////////////////////////////////////////////////////////////////////////////////////
        // create action buttons to expand, collapse and collapse all except selected menu items //
        // /////////////////////////////////////////////////////////////////////////////////////////
        final JButton jbExpandAll = new JButton(getTree().getExpandAllAction());
        jbExpandAll.setFocusable(false);
        jbExpandAll.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(final MouseEvent e) {
                jbExpandAll.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(final MouseEvent e) {
                jbExpandAll.setCursor(Cursor.getDefaultCursor());
            }
        });
        search.add(jbExpandAll, "width 16:16:16, height 16:16:16");

        final JButton jbCollapseAll = new JButton(getTree().getCollapseAllAction());
        jbCollapseAll.setFocusable(false);
        jbCollapseAll.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(final MouseEvent e) {
                jbCollapseAll.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(final MouseEvent e) {
                jbCollapseAll.setCursor(Cursor.getDefaultCursor());
            }
        });
        search.add(jbCollapseAll, "width 16:16:16, height 16:16:16");

        final JButton jbCollapseAllExceptSelected = new JButton(getTree().getCollapseAllExceptSelectedAction());
        jbCollapseAllExceptSelected.setFocusable(false);
        jbCollapseAllExceptSelected.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(final MouseEvent e) {
                jbCollapseAllExceptSelected.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(final MouseEvent e) {
                jbCollapseAllExceptSelected.setCursor(Cursor.getDefaultCursor());
            }
        });
        search.add(jbCollapseAllExceptSelected, "width 16:16:16, height 16:16:16");

        add(search, "wrap");
        final JScrollPane scroll = new JScrollPane(getTree());
        scroll.setBackground(Color.white);
        scroll.setBorder(new EmptyBorder(0, 0, 0, 0));
        scroll.getViewport().addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(final ChangeEvent e) {
                final ViewportChangeEvent event = new ViewportChangeEvent(scroll, scroll.getViewport().getViewPosition(), //
                scroll.getViewport().getExtentSize(), scroll.getSize());
                getTree().viewPortChanged(event);
            }
        });
        add(scroll);

        //	this.search = search;
        //	this.filterControl = filterControl;
    }

    public EntitiesTree2<DTM> getTree() {
        return entitiesTree;
    }

    /**
     * Create an action for selecting the first matched menu item.
     * 
     * @return
     */
    private Action createSelectItemAction(final JTree reportTree, final IFilterableModel filterableModel) {
        return new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(final ActionEvent e) {
                // iterate through all visible rows in the tree and find the first matching by filter node
                // once found select it and focus the tree
                for (int index = 0; index < reportTree.getRowCount(); index++) {
                    final TreeNode currNode = (TreeNode) reportTree.getPathForRow(index).getLastPathComponent();
                    if (currNode != null && filterableModel.matches(currNode)) {
                        reportTree.setSelectionRow(index);
                        reportTree.requestFocusInWindow();
                        break;
                    }
                }

            }
        };
    }

    /**
     * Creates clear filter action.
     * 
     * @param filerControl
     * @return
     */
    private Action createClearFilterAction(final FilterControl filerControl) {
        return new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(final ActionEvent e) {
                filerControl.clear();
            }
        };
    }
}
