package ua.com.fielden.platform.swing.egi;

import static javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION;
import static javax.swing.ListSelectionModel.SINGLE_SELECTION;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER;
import static ua.com.fielden.platform.swing.egi.models.mappings.ColumnTotals.GRAND_TOTALS_SEPARATE_FOOTER;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableColumn;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.collections.map.ListOrderedMap;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.egi.models.PropertyTableModel;

/**
 * This class is a {@link JPanel} holding EGI and providing necessary layout functionality for it (like total's editors placing and resizing)
 * 
 * @author yura
 * 
 * @param <T>
 */
public class EgiPanel<T extends AbstractEntity> extends JPanel {

    /**
     * For now it's static field, however in the future it should be configured from outside
     */
    private static final int ROW_HEIGHT = 26;

    private final EntityGridInspector<T> egi;

    private final JScrollPane egiScrollPane;

    /**
     * Mapping between property name and totals editor component
     */
    private final ListOrderedMap totalsEditors = new ListOrderedMap();

    public EgiPanel(final PropertyTableModel<T> model, final boolean sortable) {
	super();
	this.egi = createEgi(model, sortable);

	if (model.hasGrandTotalsSeparateFooter()) {
	    setLayout(new MigLayout("fill, insets 0", "[]", "[grow]0[shrink 0]0[]"));
	    fillTotalsEditors(totalsEditors, egi.getActualModel());

	    add(egiScrollPane = new JScrollPane(egi, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER), //
	    "grow, wrap");

	    final JScrollPane footerPane = new JScrollPane(createFooterPanel(egi, totalsEditors), VERTICAL_SCROLLBAR_NEVER, HORIZONTAL_SCROLLBAR_NEVER);
	    add(footerPane, "growx, wrap");

	    add(egiScrollPane.getHorizontalScrollBar(), "growx");
	    enableFooterPaneScrolling(footerPane, egiScrollPane.getHorizontalScrollBar());
	} else {
	    setLayout(new MigLayout("fill, insets 0"));
	    add(egiScrollPane = new JScrollPane(egi), "grow");
	}
    }

    /**
     * This mapping sets tool-tips for editors using propertyName -> tooltip mapping specified by <code>propertyTooltipMap</code> parameter.
     * 
     * @param propertyTooltipMap
     */
    public void setEditorsTooltips(final Map<String, String> propertyTooltipMap) {
	for (final Entry<String, String> entry : propertyTooltipMap.entrySet()) {
	    final JComponent editor = getTotalsEditor(entry.getKey());
	    if (editor instanceof JTextField) {
		((JTextField) editor).setToolTipText(entry.getValue());
	    }
	}
    }

    private static void enableFooterPaneScrolling(final JScrollPane footerPane, final JScrollBar egiHorizScrollBar) {
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

    private static <T extends AbstractEntity> void fillTotalsEditors(final ListOrderedMap totalsEditors, final PropertyTableModel<T> actualModel) {
	for (final AbstractPropertyColumnMapping<T> mapping : actualModel.getPropertyColumnMappings()) {
	    if (GRAND_TOTALS_SEPARATE_FOOTER.equals(mapping.getColumnTotals())) {
		final JTextField totalsEditor = new JTextField();
		totalsEditor.setEditable(false);
		totalsEditor.setPreferredSize(new Dimension(mapping.getSize(), ROW_HEIGHT));

		totalsEditors.put(mapping.getPropertyName(), totalsEditor);
	    } else {
		final JLabel editorStub = new JLabel();
		editorStub.setPreferredSize(new Dimension(mapping.getSize(), ROW_HEIGHT));
		totalsEditors.put(mapping.getPropertyName(), editorStub);
	    }
	}
    }

    /**
     * Creates {@link EntityGridInspector} displaying entities.
     * 
     * @param entityTableModel
     * @return
     */
    protected EntityGridInspector<T> createEgi(final PropertyTableModel<T> entityTableModel, final boolean sortable) {
	final EntityGridInspector<T> egi = new EntityGridInspector<T>(entityTableModel, sortable);
	egi.setRowHeight(ROW_HEIGHT);
	egi.setSelectionMode(SINGLE_SELECTION);
	egi.setSingleExpansion(true);

	egi.getColumnModel().getSelectionModel().setSelectionMode(SINGLE_INTERVAL_SELECTION);
	return egi;
    }

    private static <T extends AbstractEntity> JPanel createFooterPanel(final EntityGridInspector<T> egi, final ListOrderedMap totalsEditors) {
	final JPanel footer = new JPanel(new MigLayout("nogrid, insets 0"));
	for (int i = 0; i < totalsEditors.size(); i++) {
	    footer.add((JComponent) totalsEditors.getValue(i), "grow, gap 0 0 0 0");
	}
	// adding last label so that it fill the space in the viewport under vertical scroll bar
	footer.add(new JLabel() {
	    {
		setPreferredSize(new Dimension(50, 0));
	    }
	}, "grow, gap 0 0 0 0");
	addResizingListener(footer, egi, totalsEditors);
	return footer;
    }

    private static <T extends AbstractEntity> void addResizingListener(final JPanel footer, final EntityGridInspector<T> egi, final ListOrderedMap totalsEditors) {
	egi.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
	    @Override
	    public void columnAdded(final TableColumnModelEvent e) {
	    }

	    @Override
	    public void columnMarginChanged(final ChangeEvent e) {
		final TableColumn column = egi.getTableHeader().getResizingColumn();
		if (column != null) {
		    // obtained column that was resized
		    final JComponent totalsComponent = (JComponent) totalsEditors.getValue(egi.convertColumnIndexToView(column.getModelIndex()));
		    totalsComponent.setPreferredSize(new Dimension(column.getWidth(), totalsComponent.getHeight()));
		} else {
		    // couldn't determine the column that was resized - fitting all editors to column width's
		    for (int i = 0; i < totalsEditors.size(); i++) {
			final JComponent totalsComponent = (JComponent) totalsEditors.getValue(i);
			totalsComponent.setPreferredSize(new Dimension(egi.getColumnModel().getColumn(i).getWidth(), totalsComponent.getHeight()));
		    }
		}
		footer.revalidate();
	    }

	    @Override
	    public void columnMoved(final TableColumnModelEvent e) {
		final String fromPropName = (String) totalsEditors.get(e.getFromIndex());
		final JComponent fromComponent = (JComponent) totalsEditors.getValue(e.getFromIndex());

		totalsEditors.put(e.getFromIndex(), totalsEditors.get(e.getToIndex()), totalsEditors.getValue(e.getToIndex()));
		totalsEditors.put(e.getToIndex(), fromPropName, fromComponent);

		footer.removeAll();
		for (int i = 0; i < egi.getColumnCount(); i++) {
		    footer.add((JComponent) totalsEditors.getValue(i), "grow, gap 0 0 0 0");
		}
		// adding last label so that it fill the space in the viewport under vertical scroll bar
		footer.add(new JLabel() {
		    {
			setPreferredSize(new Dimension(50, 0));
		    }
		}, "grow, gap 0 0 0 0");
		footer.revalidate();
	    }

	    @Override
	    public void columnRemoved(final TableColumnModelEvent e) {
	    }

	    @Override
	    public void columnSelectionChanged(final ListSelectionEvent e) {
	    }
	});
    }

    public JComponent getTotalsEditor(final String propertyName) {
	return (JComponent) totalsEditors.get(propertyName);
    }

    public EntityGridInspector<T> getEgi() {
	return egi;
    }

    public JScrollPane getEgiScrollPane() {
	return egiScrollPane;
    }

}
