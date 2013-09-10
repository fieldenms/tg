package ua.com.fielden.platform.swing.review.report.analysis.pivot;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

import ua.com.fielden.platform.swing.egi.models.mappings.ReadonlyPropertyColumnMapping.RendererCheckBox;
import ua.com.fielden.platform.swing.egi.models.mappings.ReadonlyPropertyColumnMapping.RendererLabel;
import ua.com.fielden.platform.utils.ConverterFactory.Converter;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.EntityUtils.ShowingStrategy;

public class PivotTableCellRenderer implements TableCellRenderer {

    private final Class<?>[] types;
    private final JComponent[] views;
    private final Converter[] converters;
    private final JPanel viewPanel;

    public PivotTableCellRenderer(final int gapWidth, final Color gridColor, final Class<?>[] columnTypes) {
	types = new Class<?>[columnTypes.length];
	views = new JComponent[columnTypes.length];
	converters = new Converter[columnTypes.length];
	viewPanel = new JPanel(new GridLayout(1, columnTypes.length, gapWidth, 0));
	for(int typeIndex = 0; typeIndex < columnTypes.length; typeIndex++) {
	    types[typeIndex] = columnTypes[typeIndex];
	    converters[typeIndex] = createConverter(columnTypes[typeIndex]);
	    views[typeIndex] = createView(columnTypes[typeIndex]);
	    viewPanel.add(views[typeIndex]);
	}
	viewPanel.setBackground(gridColor);
	//viewPanel.setOpaque(false);
    }

    private Converter createConverter(final Class<?> type) {
	return EntityUtils.chooseConverterBasedUponPropertyType(type, null, ShowingStrategy.KEY_AND_DESC);
    }

    private JComponent createView(final Class<?> type) {
	return isCheckBox(type) ? new RendererCheckBox() : new RendererLabel(type);
    }

    private boolean isCheckBox(final Class<?> type) {
	return (type == Boolean.class || type == boolean.class);
    }

    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
	final Object[] values = (Object[])value;
	for(int typeIndex = 0; typeIndex < types.length; typeIndex++) {
	    // setting value
	    if (isCheckBox(types[typeIndex])) {
		((JCheckBox) views[typeIndex]).setSelected((Boolean) values[typeIndex]);
	    } else {
		((JLabel) views[typeIndex]).setText(EntityUtils.getLabelText(values[typeIndex], converters[typeIndex]));
	    }
	    if (!isSelected) {
		if (row % 2 != 0) {
		    final Color ac = UIManager.getColor("Table.alternateRowColor");
		    views[typeIndex].setBackground(new Color(ac.getRed(), ac.getGreen(), ac.getBlue()));
		} else {
		    views[typeIndex].setBackground(Color.WHITE);
		}
		views[typeIndex].setForeground(table.getForeground());
	    } else {
		final Color fg = UIManager.getColor("Table.dropCellForeground");
		final Color bg = UIManager.getColor("Table.dropCellBackground");
		views[typeIndex].setForeground(fg == null ? table.getSelectionForeground() : fg);
		views[typeIndex].setBackground(bg == null ? table.getSelectionBackground() : bg);
	    }
	}
	return viewPanel;
    }
}
