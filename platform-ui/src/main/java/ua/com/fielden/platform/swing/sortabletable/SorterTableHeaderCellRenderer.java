package ua.com.fielden.platform.swing.sortabletable;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;

import ua.com.fielden.platform.swing.review.OrderingArrow;
import ua.com.fielden.platform.swing.verticallabel.DefaultTableHeaderCellRenderer;

/**
 * Cell renderer for the table column headers. This renderer supports multiple sorting of the data in the grid
 *
 * @author oleh
 *
 */
public class SorterTableHeaderCellRenderer extends DefaultTableHeaderCellRenderer {

    private static final long serialVersionUID = -1046188131931278304L;

    private final OrderingIcon orderingIcon = new OrderingIcon();

    private final static int ICON_LEFT_INSETS = 3;
    private final static int ICON_TOP_INSETS = 3;
    private final static int ICON_RIGHT_INSETS = -3;
    private final static int ICON_BOTTOM_INSETS = 3;

    /**
     * Creates new table header cell renderer with ordering icon
     */
    public SorterTableHeaderCellRenderer() {
	setHorizontalAlignment(LEFT);
	setHorizontalTextPosition(RIGHT);
    }

    @Override
    protected Icon getIcon(final JTable table, final int column) {
	if (table.getRowSorter() == null) {
	    return null;
	}
	final int modelColumn = table.convertColumnIndexToModel(column);
	if (table.getRowSorter() instanceof PropertyTableModelRowSorter) {
	    final PropertyTableModelRowSorter rowSorter = (PropertyTableModelRowSorter) table.getRowSorter();
	    if (!rowSorter.isSortable(modelColumn)) {
		return null;
	    }
	}
	final List<? extends SortKey> sortKeys = table.getRowSorter().getSortKeys();
	for (int counter = 0; counter < sortKeys.size(); counter++) {
	    if (modelColumn == sortKeys.get(counter).getColumn()) {
		orderingIcon.setOrder(counter + 1);
		orderingIcon.setSortOrder(sortKeys.get(counter).getSortOrder());
		return orderingIcon;
	    }
	}
	orderingIcon.setSortOrder(SortOrder.UNSORTED);
	return orderingIcon;

    }

    /**
     * Icon that represents ordering arrow on the table header
     *
     * @author oleh
     *
     */
    private class OrderingIcon implements Icon {

	private final OrderingArrow orderingArrow;

	/**
	 * Creates {@link OrderingIcon} and {@link OrderingArrow}
	 */
	public OrderingIcon() {
	    orderingArrow = new OrderingArrow();
	}

	@Override
	public int getIconHeight() {
	    return (int) Math.ceil(orderingArrow.getActualHeight(getGraphics()) + ICON_TOP_INSETS + ICON_BOTTOM_INSETS);
	}

	@Override
	public int getIconWidth() {
	    return (int) Math.ceil(orderingArrow.getActualWidth(getGraphics()) + ICON_LEFT_INSETS + ICON_RIGHT_INSETS);
	}

	@Override
	public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
	    g.translate(x + ICON_LEFT_INSETS, y + ICON_TOP_INSETS);
	    orderingArrow.paintComponent(g);
	    g.translate(-x - ICON_LEFT_INSETS, -y - ICON_TOP_INSETS);
	}

	/**
	 * Returns the {@link SortOrder} of the {@link OrderingArrow}
	 *
	 * @return
	 */
	public SortOrder getSortOrder() {
	    return orderingArrow.getSortOrder();
	}

	/**
	 * Returns the order of the {@link OrderingArrow}
	 *
	 * @return
	 */
	public int getOrder() {
	    return orderingArrow.getOrder();
	}

	/**
	 * Set the {@link SortOrder} for the {@link OrderingArrow}
	 *
	 * @param sortOrder
	 */
	public void setSortOrder(final SortOrder sortOrder) {
	    orderingArrow.setSortOrder(sortOrder);
	}

	/**
	 * Set the order value for the {@link OrderingArrow} associated with this {@link OrderingIcon}
	 *
	 * @param order
	 */
	public void setOrder(final int order) {
	    orderingArrow.setOrder(order);
	}

	/**
	 * Set the indicator that determines whether {@link OrderingArrow} is highlighted or not
	 *
	 * @param mouseOver
	 */
	public void setMouseOver(final boolean mouseOver) {
	    orderingArrow.setMouseOver(mouseOver);
	}

    }

    @Override
    public Dimension getPreferredSize() {
	final Dimension labelDimension = super.getPreferredSize();
	return new Dimension(labelDimension.width, orderingIcon.getIconHeight());
    }

    @Override
    public void setMouseOver(final boolean mouseOver) {
	super.setMouseOver(mouseOver);
	orderingIcon.setMouseOver(mouseOver);
    }

    @Override
    public Insets getInsets() {
	return new Insets(3, 3, 3, 3 + (getIcon() != null ? ICON_LEFT_INSETS : 0));
    }

    @Override
    public Insets getInsets(final Insets insets) {
	return getInsets();
    }
}
