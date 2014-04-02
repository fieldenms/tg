package ua.com.fielden.platform.swing.treetable;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreeNode;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.swing.menu.filter.IFilter;
import ua.com.fielden.platform.swing.menu.filter.IFilterableModel;
import ua.com.fielden.platform.swing.menu.filter.ui.FilterControl;

public class FilterableTreeTablePanel<T extends FilterableTreeTable> extends JPanel {

    private static final long serialVersionUID = 5860204397455593391L;

    private final T treeTable;

    private final JPanel search;

    private final FilterControl filterControl;

    public FilterableTreeTablePanel(final T treeTable, final IFilter filter, final String filterCaption) {
        super(new MigLayout("fill, insets 0", "[150::,grow,fill]", "[:30:][grow,fill]"));

        this.treeTable = treeTable;

        search = new JPanel(new MigLayout("fill, insets 2 2 2 2", "[fill,grow]1[]1[]1[]", "[fill,grow]"));
        ((IFilterableModel) getTreeTable().getTreeTableModel()).addFilter(filter);
        final JTextField filterTextControl = new JTextField();
        filterControl = new FilterControl(filterTextControl, (IFilterableModel) getTreeTable().getTreeTableModel(), filterCaption + "...");
        search.add(filterControl, "grow");
        // create ENTER key stroke and a corresponding action for selecting the first matched menu item in the tree
        final KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0); // no modifiers
        final String SELECT_MENU_ITEM = "SELECT_MENU_ITEM";
        filterTextControl.getActionMap().put(SELECT_MENU_ITEM, createSelectItemAction(treeTable, (IFilterableModel) treeTable.getTreeTableModel()));
        filterTextControl.getInputMap().put(enter, SELECT_MENU_ITEM);
        // create ESC key stroke for clearing filter
        final KeyStroke esc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0); // no modifiers
        final String CLEAR_FILTER = "CLEAR_FILTER";
        filterTextControl.getActionMap().put(CLEAR_FILTER, createClearFilterAction(filterControl));
        filterTextControl.getInputMap().put(esc, CLEAR_FILTER);

        // creating expand collapse buttons
        final JButton expandButton = new JButton(treeTable.getExpandAllAction());
        expandButton.setFocusable(false);
        expandButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        final JButton collapseButton = new JButton(treeTable.getCollapseAllAction());
        collapseButton.setFocusable(false);
        collapseButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        final JButton exceptSelectedButton = new JButton(treeTable.getCollapseAllExceptSelectedAction());
        exceptSelectedButton.setFocusable(false);
        exceptSelectedButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        search.add(expandButton, "width 16:16:16, height 16:16:16");
        search.add(collapseButton, "width 16:16:16, height 16:16:16");
        search.add(exceptSelectedButton, "width 16:16:16, height 16:16:16");

        add(search, "wrap");
        final JScrollPane scroll = new JScrollPane(treeTable);
        scroll.getViewport().addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(final ChangeEvent e) {
                final ViewportChangeEvent event = new ViewportChangeEvent(scroll, scroll.getViewport().getViewPosition(), //
                scroll.getViewport().getExtentSize(), scroll.getSize());
                treeTable.viewPortChanged(event);
            }
        });
        add(scroll);
    }

    private Action createClearFilterAction(final FilterControl filterControl) {
        return new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(final ActionEvent e) {
                System.out.println("CLEAR FILTER");
                filterControl.clear();
            }
        };
    }

    private Action createSelectItemAction(final FilterableTreeTable treeTable, final IFilterableModel treeTableModel) {
        return new AbstractAction() {

            private static final long serialVersionUID = 7531625260833571520L;

            @Override
            public void actionPerformed(final ActionEvent e) {
                for (int rowCounter = 0; rowCounter < treeTable.getRowCount(); rowCounter++) {
                    final TreeNode treeNode = (TreeNode) treeTable.getPathForRow(rowCounter).getLastPathComponent();
                    if (treeNode != null && treeTableModel.matches(treeNode)) {
                        final ListSelectionModel selectionModel = treeTable.getSelectionModel();
                        selectionModel.setSelectionInterval(0, rowCounter);
                        treeTable.requestFocusInWindow();
                        break;
                    }
                }
            }

        };
    }

    public T getTreeTable() {
        return treeTable;
    }

    public JPanel getSearchPanel() {
        return search;
    }

    public FilterControl getFilterControl() {
        return filterControl;
    }

}
