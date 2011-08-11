package ua.com.fileden.platform.example.swing.treetable;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;

import org.jdesktop.swingx.JXTable;

import ua.com.fielden.platform.swing.treetable.FilterableTreeTable;
import ua.com.fielden.platform.swing.treetable.FilterableTreeTableModel;

public class ExampleTreeTable extends FilterableTreeTable {

    private static final long serialVersionUID = 144433309563947476L;

    public ExampleTreeTable(final FilterableTreeTableModel treeTableModel) {
	super(treeTableModel,false);

	getTableHeader().setReorderingAllowed(false);
	addToolTipSuportForTableHeader();
	setShowGrid(true, true);
	setGridColor(new Color(214, 217, 223));
	setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	setAutoResizeMode(JXTable.AUTO_RESIZE_OFF);
	refreshTreeTable();
    }


    // adds the tool tips to the table headers
    private void addToolTipSuportForTableHeader() {
	getTableHeader().addMouseMotionListener(new MouseMotionAdapter() {
	    @Override
	    public void mouseMoved(final MouseEvent e) {
		final int vColIndex = columnAtPoint(e.getPoint());
		final JTableHeader header = getTableHeader();

		if (vColIndex >= 0) {
		    header.setToolTipText("column tooltip "+vColIndex);

		}
	    }
	});
    }

    @Override
    public String getToolTipText(final MouseEvent event) {
	final int row = rowAtPoint(event.getPoint());
	final int col = columnAtPoint(event.getPoint());
	if (row >= 0 && col >= 0) {
	    final ExampleTreeTableNode node = (ExampleTreeTableNode) getPathForRow(row).getLastPathComponent();
	    return node.getValueAt(col).toString();
	}
	return super.getToolTipText(event);
    }

    private void refreshTreeTable() {
	if (getModel() instanceof AbstractTableModel) {
	    ((AbstractTableModel) getModel()).fireTableStructureChanged();
	}
    }
}
