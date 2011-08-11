package ua.com.fielden.platform.swing.pivot.analysis.treetable;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import ua.com.fielden.platform.swing.egi.models.mappings.ReadonlyPropertyColumnMapping.RendererCheckBox;
import ua.com.fielden.platform.swing.egi.models.mappings.ReadonlyPropertyColumnMapping.RendererLabel;
import ua.com.fielden.platform.swing.utils.RenderingDecorator;
import ua.com.fielden.platform.utils.ConverterFactory.Converter;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.EntityUtils.ShowingStrategy;

public class PivotTableCellRenderer implements TableCellRenderer {

    private final Class<?> type;

    private final JComponent view;

    private final Converter converter;

    public PivotTableCellRenderer(final Class<?> type) {
	this.type = type;
	this.view = createView();
	this.converter = createConverter();
    }

    private Converter createConverter() {
	return EntityUtils.chooseConverterBasedUponPropertyType(type, null, ShowingStrategy.KEY_AND_DESC);
    }

    private JComponent createView() {
	return isCheckBox() ? new RendererCheckBox() : new RendererLabel(type);
    }

    private boolean isCheckBox() {
	return (type == Boolean.class || type == boolean.class);
    }

    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {

	// setting value
	if (isCheckBox()) {
	    ((JCheckBox) view).setSelected((Boolean) value);
	} else {
	    ((JLabel) view).setText(EntityUtils.getLabelText(value, converter));
	}

	RenderingDecorator.decorateRenderer(view, table, isSelected, hasFocus, row, column);

	return view;
    }

}
