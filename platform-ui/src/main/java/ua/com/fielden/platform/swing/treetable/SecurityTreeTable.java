package ua.com.fielden.platform.swing.treetable;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Enumeration;

import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.TreeTableNode;

import ua.com.fielden.platform.swing.actions.Command;
import ua.com.fielden.platform.swing.verticallabel.MouseDefaultHeaderHandler;
import ua.com.fielden.platform.swing.verticallabel.VerticalTableHeaderCellRenderer;

/**
 * Extends the {@link JXTreeTable} class. Also provides such features like Vertical table header, special cell renderer for tree model, filtering and others.
 * 
 * 
 * @author oleh
 * 
 */
public class SecurityTreeTable extends FilterableTreeTable {

    private static final long serialVersionUID = -6812167524733315874L;

    /**
     * Creates new instance of {@link SecurityTreeTable} with specified {@link SecurityTreeTableModel}
     * 
     * @param model
     */
    public SecurityTreeTable(final FilterableTreeTableModel model) {
        super(model, false);

        for (int counter = 0; counter < getColumnCount(); counter++) {
            setVerticalHeaderFor(counter);
        }

        getColumnModel().addColumnModelListener(new TableColumnModelListener() {

            @Override
            public void columnAdded(final TableColumnModelEvent e) {
                setVerticalHeaderFor(e.getToIndex());
            }

            @Override
            public void columnMarginChanged(final ChangeEvent e) {

            }

            @Override
            public void columnMoved(final TableColumnModelEvent e) {

            }

            @Override
            public void columnRemoved(final TableColumnModelEvent e) {

            }

            @Override
            public void columnSelectionChanged(final ListSelectionEvent e) {

            }

        });
        final JPopupMenu popupMenu = new JPopupMenu();
        final PopupMenuListener popupListener = new PopupMenuListener(popupMenu);
        addMouseListener(popupListener);
        popupMenu.add(new JMenuItem(createCheckAction(popupListener, "Allow all", "Allow all", true)));
        popupMenu.add(new JMenuItem(createCheckAction(popupListener, "Deny all", "Deny all", false)));
        final MouseDefaultHeaderHandler mouseHandler = new MouseDefaultHeaderHandler();
        getTableHeader().addMouseMotionListener(mouseHandler);
        getTableHeader().addMouseListener(mouseHandler);
        getTableHeader().setReorderingAllowed(false);
        addToolTipSuportForTableHeader();
        setShowGrid(true, true);
        setGridColor(new Color(214, 217, 223));
        setHorizontalScrollEnabled(true);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private class PopupMenuListener extends MouseAdapter {

        private final JPopupMenu popupMenu;

        private Point mousePosition;

        public PopupMenuListener(final JPopupMenu menu) {
            this.popupMenu = menu;
        }

        @Override
        public void mousePressed(final MouseEvent e) {
            showMenu(e);
        }

        @Override
        public void mouseReleased(final MouseEvent e) {
            showMenu(e);
        }

        private void showMenu(final MouseEvent e) {
            if (e.isPopupTrigger() && SecurityTreeTable.this.isEnabled()) {
                final Point p = new Point(e.getX(), e.getY());
                final int col = SecurityTreeTable.this.columnAtPoint(p);
                final int row = SecurityTreeTable.this.rowAtPoint(p);

                if (row >= 0 && row < SecurityTreeTable.this.getRowCount() && col >= 0 && col < SecurityTreeTable.this.getColumnCount()) {
                    popupMenu.show(SecurityTreeTable.this, p.x, p.y);
                    mousePosition = p;
                }
            }
        }

        public Point getMousePosition() {
            return mousePosition;
        }

    }

    // adds the tool tips to the table headers
    private void addToolTipSuportForTableHeader() {
        getTableHeader().addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(final MouseEvent e) {
                final int vColIndex = columnAtPoint(e.getPoint());
                final JTableHeader header = getTableHeader();

                if (vColIndex >= 0) {
                    final SecurityTreeTableModel treeTableModel = (SecurityTreeTableModel) ((FilterableTreeTableModel) getTreeTableModel()).getOriginModel();
                    header.setToolTipText(treeTableModel.getColumnDesc(convertColumnIndexToModel(vColIndex)));

                }
            }
        });
    }

    private Action createCheckAction(final PopupMenuListener menuHandler, final String name, final String description, final boolean checked) {
        return new Command<Void>(name) {

            private static final long serialVersionUID = 1L;

            {
                putValue(Action.SHORT_DESCRIPTION, description);
            }

            @Override
            protected Void action(final ActionEvent e) throws Exception {
                return null;
            }

            @Override
            protected void postAction(final Void value) {
                super.postAction(value);
                final TreePath treePath = getPathForLocation(menuHandler.getMousePosition().x, menuHandler.getMousePosition().y);
                if (treePath == null) {
                    return;
                }
                final int column = columnAtPoint(menuHandler.getMousePosition());
                if (column < 0 || column >= getColumnCount()) {
                    return;
                }
                if (isHierarchical(column)) {
                    selectTree(treePath, checked, 0, getColumnCount() - 1);
                } else {
                    selectTree(treePath, checked, column, column);
                }
                if (getModel() instanceof AbstractTableModel) {
                    ((AbstractTableModel) getModel()).fireTableDataChanged();
                }
            }

            @SuppressWarnings("rawtypes")
            private void selectTree(final TreePath treePath, final boolean checked, final int columnStart, final int columnFinish) {
                final TreeTableNode node = (TreeTableNode) treePath.getLastPathComponent();
                for (int columnCounter = columnStart; columnCounter <= columnFinish; columnCounter++) {
                    if (!isHierarchical(columnCounter)) {
                        node.setValueAt(checked, columnCounter);
                    }
                }
                if (node.getChildCount() >= 0) {
                    for (final Enumeration childrenEnum = node.children(); childrenEnum.hasMoreElements();) {
                        final TreeTableNode n = (TreeTableNode) childrenEnum.nextElement();
                        final TreePath path = treePath.pathByAddingChild(n);
                        selectTree(path, checked, columnStart, columnFinish);
                    }
                }
            }

        };
    }

    @Override
    public String getToolTipText(final MouseEvent event) {
        final int row = rowAtPoint(event.getPoint());
        final int col = columnAtPoint(event.getPoint());
        if (row >= 0 && col >= 0) {
            if (isHierarchical(col)) {
                final SecurityTreeTableNode node = (SecurityTreeTableNode) getPathForRow(row).getLastPathComponent();
                return node.getLongDesc();
            } else {
                return getValueAt(row, col).toString();
            }
        }
        return super.getToolTipText(event);
    }

    /**
     * set the vertical direction of the column header
     * 
     * @param column
     */
    public void setVerticalHeaderFor(final int column) {
        final TableColumn tableColumn = getColumnModel().getColumn(column);
        if (!(tableColumn.getHeaderRenderer() instanceof VerticalTableHeaderCellRenderer)) {
            if (!isHierarchical(column)) {
                getColumn(column).setHeaderRenderer(new VerticalTableHeaderCellRenderer());
            }
        }
    }

    /**
     * set the horizontal direction of the column header
     * 
     * @param column
     */
    public void setHorizontalHeaderFor(final int column) {
        getColumn(column).setHeaderRenderer(null);
    }

}
