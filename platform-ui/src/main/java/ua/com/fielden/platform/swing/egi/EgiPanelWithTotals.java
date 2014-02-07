package ua.com.fielden.platform.swing.egi;

import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

public class EgiPanelWithTotals<T extends AbstractEntity<?>> extends JPanel {

    private static final long serialVersionUID = -1816545445234290027L;

    private final EntityGridInspector<T> egi;

    /**
     * Holds the total editors.
     */
    private final List<Pair<ITotalCalculator<?, T>, JTextField>> totalEditors = new ArrayList<>();

    public EgiPanelWithTotals(final EntityGridInspector<T> egi, final TotalBuilder<T> totalBuilder) {
	this.egi = egi;
	if (!totalBuilder.isEmpty()) {
	    setLayout(new MigLayout("fill, insets 0", "[]", "[grow]0[shrink 0]0[]"));

	    final JScrollPane egiScrollPane = new JScrollPane(egi, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);
	    add(egiScrollPane, "grow, wrap");

	    final JScrollPane footerPane = new JScrollPane(createFooterPanel(totalBuilder), VERTICAL_SCROLLBAR_NEVER, HORIZONTAL_SCROLLBAR_NEVER);
	    add(footerPane, "growx, wrap");

	    add(egiScrollPane.getHorizontalScrollBar(), "growx");
	    enableFooterPaneScrolling(footerPane, egiScrollPane.getHorizontalScrollBar());
	    egi.getModel().addTableModelListener(createTotalRefreshListener());
	} else {
	    setLayout(new MigLayout("fill, insets 0"));
	    add(new JScrollPane(egi), "grow");
	}

    }

    private TableModelListener createTotalRefreshListener() {
	return new TableModelListener() {

	    @Override
	    public void tableChanged(final TableModelEvent e) {
		final List<T> entities = egi.getActualModel().instances();
		for (final Pair<ITotalCalculator<?, T>, JTextField> total : totalEditors) {
		    setValueForTotalEditor(total.getKey().calculate(entities), total.getValue());
		}
	    }
	};
    }

    /**
     * Set the specified value for the given editor.
     *
     * @param value
     * @param editor
     */
    private void setValueForTotalEditor(final Object value, final JTextField editor) {
	if (value != null) {
	    final Class<?> valueClass = value.getClass();
	    final String totalsStrValue = EntityUtils.toString(value, valueClass);
	    editor.setText(totalsStrValue);
	    editor.setCaretPosition(0);
	    if (Number.class.isAssignableFrom(valueClass) || Money.class.isAssignableFrom(valueClass) || valueClass == int.class || valueClass == double.class) {
		editor.setHorizontalAlignment(JTextField.RIGHT);
	    }
	}
    }

    private Component createFooterPanel(final TotalBuilder<T> totalBuilder) {
	final List<? extends AbstractPropertyColumnMapping<?>> columns = egi.getActualModel().getPropertyColumnMappings();
	final int colNumber = columns.size();
	final JPanel footer = new JPanel(new MigLayout("nogrid, insets 0"));
	final List<JPanel> totalComponents = new ArrayList<JPanel>();
	totalEditors.clear();
	for (int columnIndex = 0; columnIndex < colNumber; columnIndex++) {
	    final List<ITotalCalculator<?, T>> totals = totalBuilder.getCalculators(columns.get(columnIndex).getPropertyName());
	    final JPanel totalPanel = createTotalPanel(totals, columns.get(columnIndex).getSize());
	    footer.add(totalPanel, "grow, gap 0 0 0 0");
	    totalComponents.add(totalPanel);
	}
	// adding last label so that it fill the space in the viewport under vertical scroll bar
	final JPanel stubPanel = createTotalPanel(null, 50);
	footer.add(stubPanel, "grow, gap 0 0 0 0");
	addResizingListener(footer, totalComponents);
	return footer;
    }

    private JPanel createTotalPanel(final List<ITotalCalculator<?, T>> totals, final Integer size) {
	final JPanel totalPanel = new JPanel(new MigLayout("fill, insets 0","[fill, grow]", "0[t]0"));
	totalPanel.setPreferredSize(new Dimension(size, 0));
	if (totals != null) {
	    for(int totalIndex = 0; totalIndex < totals.size() - 1; totalIndex++){
		final JTextField totalEditor = createTotalEditor(totals.get(totalIndex), size);
		totalPanel.add(totalEditor, "wrap");
		totalEditors.add(new Pair<ITotalCalculator<?, T>,JTextField>(totals.get(totalIndex), totalEditor));
	    }
	    final JTextField totalEditor = createTotalEditor(totals.get(totals.size()-1), size);
	    totalPanel.add(totalEditor);
	    totalEditors.add(new Pair<ITotalCalculator<?, T>,JTextField>(totals.get(totals.size()-1), totalEditor));
	}
	return totalPanel;
    }

    private JTextField createTotalEditor(final ITotalCalculator<?, T> iTotalCalculator, final Integer size) {
	final JTextField totalsEditor = new JTextField();
	totalsEditor.setPreferredSize(new Dimension(size, egi.getRowHeight()));
	totalsEditor.setEditable(false);
	totalsEditor.setToolTipText(iTotalCalculator.getDescription());
	return totalsEditor;
    }

    /**
     * Adds resize listener to table column that handles column size changed and column moved events.
     *
     * @param footer
     * @param totalComponents
     */
    private void addResizingListener(final JPanel footer, final List<JPanel> totalComponents) {
	egi.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
	    @Override
	    public void columnAdded(final TableColumnModelEvent e) {
	    }

	    @Override
	    public void columnMarginChanged(final ChangeEvent e) {
		final TableColumn column = egi.getTableHeader().getResizingColumn();
		if (column != null) {
		    // obtained column that was resized
		    final JPanel columnComponents = totalComponents.get(egi.convertColumnIndexToView(column.getModelIndex()));
		    columnComponents.setPreferredSize(new Dimension(column.getWidth(), columnComponents.getHeight()));
		} else {
		    // couldn't determine the column that was resized - fitting all editors to column width's
		    final int columnNumber = egi.getActualModel().getPropertyColumnMappings().size();
		    for(int columnIndex = 0; columnIndex < columnNumber; columnIndex++){
			final JPanel totalPanel = totalComponents.get(columnIndex);
			totalPanel.setPreferredSize(new Dimension(egi.getColumnModel().getColumn(columnIndex).getWidth(), totalPanel.getHeight()));
		    }
		}
		footer.revalidate();
	    }

	    @Override
	    public void columnMoved(final TableColumnModelEvent e) {
		if(e.getFromIndex() != e.getToIndex()){

		    final JPanel fromColumn = totalComponents.remove(e.getFromIndex());
		    totalComponents.add(e.getToIndex(), fromColumn);

		    footer.removeAll();
		    for(final JPanel totalPanel : totalComponents){
			footer.add(totalPanel, "grow, gap 0 0 0 0");
		    }
		    footer.revalidate();
		}
	    }

	    @Override
	    public void columnRemoved(final TableColumnModelEvent e) {
	    }

	    @Override
	    public void columnSelectionChanged(final ListSelectionEvent e) {
	    }
	});
    }

    /**
     * Enables total panel scrolling.
     *
     * @param footerPane
     * @param egiHorizScrollBar
     */
    private void enableFooterPaneScrolling(final JScrollPane footerPane, final JScrollBar egiHorizScrollBar) {
	egiHorizScrollBar.addAdjustmentListener(new AdjustmentListener() {
	    @Override
	    public void adjustmentValueChanged(final AdjustmentEvent e) {
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
			footerPane.getViewport().setViewPosition(new Point(egiHorizScrollBar.getValue(), 0));
		    }
		});
	    }
	});
    }
}
