package ua.com.fielden.platform.swing.checkboxlist;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SortOrder;

import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.swing.review.OrderingArrow;
import ua.com.fielden.platform.utils.Pair;

public class SortableListCellRenderer<E> extends JPanel implements ListCellRenderer<E>, ISortableListCellRenderer<E> {

    private static final long serialVersionUID = -2031455406156836074L;

    protected final OrderingArrow arrow;

    private int totalCellWidth;

    protected final DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

    public SortableListCellRenderer(final SortableList<E> list){
	super(new FlowLayout(FlowLayout.LEFT, 0, 0));
	arrow = new OrderingArrow();
	add(defaultRenderer);
	add(arrow);
	updateCellWidth(list);
    }

    protected void updateCellWidth(final SortableList<E> list) {
	int cellWidth = 0;
	for (int index = 0; index < list.getModel().getSize(); index++) {
	    final int currentCellWidth = defaultRenderer.getListCellRendererComponent(list, list.getModel().getElementAt(index), index, false, false).getPreferredSize().width;
	    if (currentCellWidth > cellWidth) {
		cellWidth = currentCellWidth;
	    }
	}
	totalCellWidth = cellWidth + arrow.getMinimumSize().width + 10;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Component getListCellRendererComponent(final JList<? extends E> list, final E value, final int index, final boolean isSelected, final boolean cellHasFocus) {
	if (list instanceof SortableList) {
	    final SortableList<E> sortingList = (SortableList<E>) list;
	    defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
	    setBackground(new Color(defaultRenderer.getBackground().getRGB()));
	    setBorder(defaultRenderer.getBorder());
	    defaultRenderer.setOpaque(false);
	    defaultRenderer.setBorder(BorderFactory.createEmptyBorder());
	    if (sortingList.getSortingModel().isSortable(value)) {
		arrow.setVisible(true);
		arrow.setOrder(0);
		arrow.setSortOrder(SortOrder.UNSORTED);
		final List<Pair<E, Ordering>> sortItems = sortingList.getSortingModel().getSortObjects();
		for(int sortIndex = 0; sortIndex < sortItems.size(); sortIndex++){
		    final Pair<E, Ordering> orderItem = sortItems.get(sortIndex);
		    if(orderItem.getKey().equals(value)){
			arrow.setOrder(sortIndex + 1);
			arrow.setSortOrder(sortOrder(orderItem.getValue()));
		    }
		}
	    } else {
		arrow.setVisible(false);
	    }
	    return this;
	}
	return defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }

    /**
     * Returns the {@link SortOrder} instance for specified {@link Ordering}.
     *
     * @param value
     * @return
     */
    static SortOrder sortOrder(final Ordering value) {
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
    public void doLayout() {
	final int rendererHeight = getPreferredSize().height;
	defaultRenderer.doLayout();
	defaultRenderer.setSize(defaultRenderer.getPreferredSize());
	final Dimension rSize = defaultRenderer.getSize();
	defaultRenderer.setLocation(0, (int) Math.ceil(Math.abs(rendererHeight - rSize.getHeight()) / 2.0));
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
