package ua.com.fielden.platform.swing.components;

import java.awt.Component;
import java.text.DateFormat;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

/**
 * Custom date table cell renderer that preserves the time portion of the date.
 * 
 * @author nc
 * 
 */
public class DateTimeCellRenderer implements TableCellRenderer {

    private final TableCellRenderer delegate;

    public DateTimeCellRenderer(final TableCellRenderer delegate) {
        this.delegate = delegate;
    }

    public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        final Component c = delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (value instanceof Date && c instanceof JLabel) {
            ((JLabel) c).setText(DateFormat.getDateTimeInstance().format(value));
            ((JLabel) c).setHorizontalAlignment(SwingConstants.RIGHT);
        }
        return c;
    }
}
