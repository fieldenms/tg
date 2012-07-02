package ua.com.fielden.platform.swing.checkboxlist;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

import javax.swing.JList;
import javax.swing.JToggleButton;
import javax.swing.SortOrder;

import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.swing.review.OrderingArrow;
import ua.com.fielden.platform.utils.Pair;

public class SortingCheckboxListCellRenderer<T> extends CheckboxListCellRenderer<T> implements SortingCheckingListCellRenderer<T> {

    private static final long serialVersionUID = 681138672137969031L;

    protected final OrderingArrow arrow;

    private int totalCellWidth;

    public SortingCheckboxListCellRenderer(final SortingCheckboxList<T> list, final JToggleButton toggleButton) {
	super(toggleButton);
	arrow = new OrderingArrow();
	removeAll();
	add(toggleButton);
	add(defaultRenderer);
	add(arrow);
	updateCellWidth(list);
    }

    protected void updateCellWidth(final SortingCheckboxList<T> list) {
	int cellWidth = 0;
	for (int index = 0; index < list.getModel().getSize(); index++) {
	    final int currentCellWidth = defaultRenderer.getListCellRendererComponent(list, list.getModel().getElementAt(index), index, false, false).getPreferredSize().width;
	    if (currentCellWidth > cellWidth) {
		cellWidth = currentCellWidth;
	    }
	}
	totalCellWidth = cellWidth + toggleButton.getPreferredSize().width + arrow.getMinimumSize().width + 10;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Component getListCellRendererComponent(final JList list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
	if (list instanceof SortingCheckboxList) {
	    final SortingCheckboxList<T> sortingList = (SortingCheckboxList<T>) list;
	    if (sortingList.isValueChecked((T) value) && sortingList.getSortingModel().isSortable((T) value)) {
		arrow.setVisible(true);
		arrow.setOrder(0);
		arrow.setSortOrder(SortOrder.UNSORTED);
		final List<Pair<T, Ordering>> sortItems = sortingList.getSortingModel().getSortObjects();
		for(int sortIndex = 0; sortIndex < sortItems.size(); sortIndex++){
		    final Pair<T, Ordering> orderItem = sortItems.get(sortIndex);
		    if(orderItem.getKey().equals(value)){
			arrow.setOrder(sortIndex + 1);
			arrow.setSortOrder(sortOrder(orderItem.getValue()));
		    }
		}
	    } else {
		arrow.setVisible(false);
	    }
	}
	return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }

    /**
     * Returns the {@link SortOrder} instance for specified {@link Ordering}.
     *
     * @param value
     * @return
     */
    private SortOrder sortOrder(final Ordering value) {
	switch (value) {
	case ASCENDING:
	    return SortOrder.ASCENDING;
	case DESCENDING:
	    return SortOrder.DESCENDING;
	}
	return SortOrder.UNSORTED;
    }

    @Override
    public boolean isOnOrderingArrow(final int x, final int y) {
	final Rectangle position = arrow.getBounds();
	if (x < position.x || y < 0) {
	    return false;
	}
	final Dimension minSize = arrow.getMinimumSize();
	if (position.x + minSize.width < x || position.y + minSize.height < y) {
	    return false;
	}
	return true;
    }

    @Override
    public void paint(final Graphics g) {
	super.paint(g);
	if (arrow.isVisible()) {
	    final Point arrowLocation = arrow.getLocation();
	    g.translate(arrowLocation.x, arrowLocation.y);
	    arrow.paintComponent(g);
	    g.translate(-arrowLocation.x, -arrowLocation.y);
	}
    }

    @Override
    public void doLayout() {
	final int rendererHeight = getPreferredSize().height;
	final Dimension toggleButtonSize = toggleButton.getPreferredSize();
	toggleButton.doLayout();
	toggleButton.setSize(toggleButtonSize);
	toggleButton.setLocation(0, (int) Math.ceil(Math.abs(rendererHeight - toggleButtonSize.getHeight()) / 2.0));
	defaultRenderer.doLayout();
	defaultRenderer.setSize(defaultRenderer.getPreferredSize());
	final Dimension rSize = defaultRenderer.getSize();
	defaultRenderer.setLocation(toggleButtonSize.width, (int) Math.ceil(Math.abs(rendererHeight - rSize.getHeight()) / 2.0));
	final Dimension arrowSize = arrow.getMinimumSize();
	arrow.setLocation(totalCellWidth - arrowSize.width, (int) Math.ceil(Math.abs(rendererHeight - arrowSize.getHeight()) / 2.0));
	arrow.setSize(arrowSize);
    }

    @Override
    public Dimension getPreferredSize() {
	final Dimension arrowSize = arrow.getMinimumSize();
	final int height = arrowSize.height + 6;
	return new Dimension(totalCellWidth, height);
    }

}
