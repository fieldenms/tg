/**
 *
 */
package ua.com.fielden.platform.swing.utils;

import java.awt.Color;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

import ua.com.fielden.platform.swing.components.ValidationLayer;

/**
 * Class with two methods that applies, corresponding to current LnF, decoration for table renderers and editors <br>
 * <br>
 * Note : code is copy-pasted from {@link DefaultTableCellRenderer#getTableCellRendererComponent(JTable, Object, boolean, boolean, int, int)} and slightly refactored
 * 
 * @author Yura
 */
public class RenderingDecorator {

    /**
     * Let's hide the default constructor -- this is a static class.
     */
    private RenderingDecorator() {
    }

    /**
     * An empty <code>Border</code>. This field might not be used. To change the <code>Border</code> used by this renderer override the <code>getTableCellRendererComponent</code>
     * method and set the border of the returned component directly.
     */
    private static final Border SAFE_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);
    private static final Border DEFAULT_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);
    private static Border noFocusBorder = DEFAULT_NO_FOCUS_BORDER;

    private static Border getNoFocusBorder() {
        final Border border = UIManager.getBorder("Table.cellNoFocusBorder");
        if (System.getSecurityManager() != null) {
            if (border != null) {
                return border;
            }
            return SAFE_NO_FOCUS_BORDER;
        } else if (border != null) {
            if (noFocusBorder == null || noFocusBorder == DEFAULT_NO_FOCUS_BORDER) {
                return border;
            }
        }
        return noFocusBorder;
    }

    /**
     * Decorates passed component to look like cell renderer for passed table.<br>
     * <br>
     * Note : code copy-pasted from {@link DefaultTableCellRenderer#getTableCellRendererComponent(JTable, Object, boolean, boolean, int, int)} and slightly refactored
     * 
     * @param component
     * @param table
     * @param isSelected
     * @param hasFocus
     * @param row
     * @param column
     */
    public static void decorateRenderer(final JComponent component, final JTable table, boolean isSelected, final boolean hasFocus, final int row, final int column) {
        if (component instanceof ValidationLayer) {
            decorateRenderer(((ValidationLayer) component).getView(), table, isSelected, hasFocus, row, column);
        } else {

            Color fg = null;
            Color bg = null;

            final JTable.DropLocation dropLocation = table.getDropLocation();
            if (dropLocation != null && !dropLocation.isInsertRow() && !dropLocation.isInsertColumn() && dropLocation.getRow() == row && dropLocation.getColumn() == column) {

                fg = UIManager.getColor("Table.dropCellForeground");
                bg = UIManager.getColor("Table.dropCellBackground");

                isSelected = true;
            }

            if (isSelected) {
                component.setForeground(fg == null ? table.getSelectionForeground() : fg);
                component.setBackground(bg == null ? table.getSelectionBackground() : bg);
            } else {
                Color background = table.getBackground();
                if (background == null || background instanceof javax.swing.plaf.UIResource) {
                    final Color alternateColor = UIManager.getColor("Table.alternateRowColor");
                    if (alternateColor != null && row % 2 != 0) {
                        background = alternateColor;
                    }
                }
                component.setForeground(table.getForeground());
                component.setBackground(background);
            }

            component.setFont(table.getFont());

            if (hasFocus) {
                Border border = null;
                if (isSelected) {
                    border = UIManager.getBorder("Table.focusSelectedCellHighlightBorder");
                }
                if (border == null) {
                    border = UIManager.getBorder("Table.focusCellHighlightBorder");
                }
                component.setBorder(border);

                if (!isSelected && table.isCellEditable(row, column)) {
                    Color col = UIManager.getColor("Table.focusCellForeground");
                    if (col != null) {
                        component.setForeground(col);
                    }
                    col = UIManager.getColor("Table.focusCellBackground");
                    if (col != null) {
                        component.setBackground(col);
                    }
                }
            } else {
                component.setBorder(getNoFocusBorder());
            }
        }
    }

    /**
     * Decorates passed component to look like cell editor for passed table. Decorates like {@link #decorateRenderer(JComponent, JTable, boolean, boolean, int, int)} but assuming
     * that cell is selected and has focus
     * 
     * @param component
     * @param table
     */
    @SuppressWarnings("unchecked")
    public static void decorateEditor(final JComponent component, final JTable table) {
        // TODO this should be implemented after far-binding for read-only boolean properties would be implemented
        if ((component instanceof ValidationLayer) && ((ValidationLayer) component).getView() instanceof JCheckBox) {
            final JCheckBox editor = ((ValidationLayer<JCheckBox>) component).getView();
            editor.setOpaque(true);
            decorateEditor(editor, table);
        }
        component.setForeground(table.getSelectionForeground());
        component.setBackground(table.getSelectionBackground());

        component.setFont(table.getFont());

        Border border = UIManager.getBorder("Table.focusSelectedCellHighlightBorder");
        border = UIManager.getBorder("Table.focusSelectedCellHighlightBorder");
        if (border == null) {
            border = UIManager.getBorder("Table.focusCellHighlightBorder");
        }
        component.setBorder(border);
    }

}
