package ua.com.fielden.platform.swing.checkboxlist;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SortOrder;

import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.swing.review.OrderingArrow;
import ua.com.fielden.platform.utils.Pair;

public abstract class SortingCheckboxListCellRenderer<T> extends JPanel implements ListCellRenderer<T>, ISortableCheckboxListCellRenderer<T> {

    private static final long serialVersionUID = 681138672137969031L;

    private final SortingCheckboxList<T> sortingCheckboxList;

    protected final List<JCheckBox> toggleButtons;
    protected final DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
    protected final OrderingArrow arrow;

    private int totalCellWidth;

    public SortingCheckboxListCellRenderer(final SortingCheckboxList<T> list) {
	super(new FlowLayout(FlowLayout.LEFT, 0, 0));
	this.sortingCheckboxList = list;
	this.arrow = new OrderingArrow();
	this.toggleButtons = new ArrayList<>(list.getCheckingModelCount());
	for(int modelIndex = 0; modelIndex < list.getCheckingModelCount(); modelIndex++){
	    toggleButtons.add(new JCheckBox());
	    add(toggleButtons.get(modelIndex));
	}
	add(defaultRenderer);
	add(arrow);
	updateCellWidth(list);
    }

    /**
     * Updates the maximum cell width of the list.
     *
     * @param list
     */
    protected void updateCellWidth(final SortingCheckboxList<T> list) {
	if(list.getSortingModel() == null){
	    totalCellWidth = 0;
	} else {
	    totalCellWidth = getMaxCellWidth(list) + getButtonsWidth() + arrow.getMinimumSize().width + 10;
	}
    }

    /**
     * Returns the maximum cell width for the list.
     *
     * @param list
     * @return
     */
    private int getMaxCellWidth(final SortingCheckboxList<T> list){
	int cellWidth = 0;
	for (int index = 0; index < list.getModel().getSize(); index++) {
	    final int currentCellWidth = defaultRenderer.getListCellRendererComponent(list, list.getModel().getElementAt(index), index, false, false).getPreferredSize().width;
	    if (currentCellWidth > cellWidth) {
		cellWidth = currentCellWidth;
	    }
	}
	return cellWidth;
    }

    /**
     * Returns the width of the check box array.
     *
     * @return
     */
    private int getButtonsWidth(){
	int buttonPrefferedWidth = 0;
	for (int buttonIndex = 0; buttonIndex < toggleButtons.size(); buttonIndex++) {
	    buttonPrefferedWidth += toggleButtons.get(buttonIndex).getPreferredSize().width;
	}
	return buttonPrefferedWidth;
    }

    /**
     * Returns the maximum check box height.
     *
     * @return
     */
    private int getMaximumButtonHeight(){
	int buttonHeight = 0;
	for (int buttonIndex = 0; buttonIndex < toggleButtons.size(); buttonIndex++) {
	    final int toggleButtonHeight = toggleButtons.get(buttonIndex).getPreferredSize().height;
	    if(buttonHeight < toggleButtonHeight){
		buttonHeight = toggleButtonHeight;
	    }
	}
	return buttonHeight;
    }

    @Override
    public int getHotSpot(final int x, final int y) {
	for (int buttonIndex = 0; buttonIndex < toggleButtons.size(); buttonIndex++) {
	    if (toggleButtons.get(buttonIndex).getBounds().contains(x, y)) {
		return buttonIndex;
	    }
	}
	return -1;
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

    @SuppressWarnings("unchecked")
    @Override
    public Component getListCellRendererComponent(final JList<? extends T> list, final T value, final int index, final boolean isSelected, final boolean cellHasFocus) {
	if (list instanceof SortingCheckboxList) {
	    final SortingCheckboxList<T> checkboxList = (SortingCheckboxList<T>) list;
	    defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
	    setBackground(new Color(defaultRenderer.getBackground().getRGB()));
	    setBorder(defaultRenderer.getBorder());
	    defaultRenderer.setOpaque(false);
	    defaultRenderer.setBorder(BorderFactory.createEmptyBorder());
	    initCheckBoxes(checkboxList, value);
	    initOrderingArrow(checkboxList, value);
	    return this;
	}
	return defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }

    private void initOrderingArrow(final SortingCheckboxList<T> checkboxList, final T value) {
	if (isSortingAvailable(value)) {
	    arrow.setVisible(true);
	    arrow.setOrder(0);
	    arrow.setSortOrder(SortOrder.UNSORTED);
	    final List<Pair<T, Ordering>> sortItems = checkboxList.getSortingModel().getSortObjects();
	    for (int sortIndex = 0; sortIndex < sortItems.size(); sortIndex++) {
		final Pair<T, Ordering> orderItem = sortItems.get(sortIndex);
		if (orderItem.getKey().equals(value)) {
		    arrow.setOrder(sortIndex + 1);
		    arrow.setSortOrder(sortOrder(orderItem.getValue()));
		}
	    }
	} else {
	    arrow.setVisible(false);
	}
    }

    private void initCheckBoxes(final SortingCheckboxList<T> checkboxList, final T value) {
	for(int index  = 0; index < toggleButtons.size(); index++){
	if (checkboxList.isValueChecked(value, index)) {
		toggleButtons.get(index).setSelected(true);
	    } else {
		toggleButtons.get(index).setSelected(false);
	    }
	}
    }

    @Override
    public void doLayout() {
	final int rendererHeight = getPreferredSize().height;
	int xLocation = 0;
	for (final JCheckBox toggleButton : toggleButtons) {
	    final Dimension toggleButtonSize = toggleButton.getPreferredSize();
	    toggleButton.doLayout();
	    toggleButton.setSize(toggleButtonSize);
	    toggleButton.setLocation(xLocation, (int) Math.ceil(Math.abs(rendererHeight - toggleButtonSize.getHeight()) / 2.0));
	    xLocation += toggleButtonSize.width;
	}
	defaultRenderer.doLayout();
	defaultRenderer.setSize(defaultRenderer.getPreferredSize());
	final Dimension rSize = defaultRenderer.getSize();
	defaultRenderer.setLocation(xLocation, (int) Math.ceil(Math.abs(rendererHeight - rSize.getHeight()) / 2.0));
	if (sortingCheckboxList.getSortingModel() != null) {
	    final Dimension arrowSize = arrow.getMinimumSize();
	    arrow.setLocation(totalCellWidth - arrowSize.width, (int) Math.ceil(Math.abs(rendererHeight - arrowSize.getHeight()) / 2.0));
	    arrow.setSize(arrowSize);
	}
    }

    @Override
    public Dimension getPreferredSize() {
	if(sortingCheckboxList.getSortingModel() == null){
	    return getPrefferedSizeWithoutOrdering();
	} else {
	    return getPrefferedSizeWithOrdering();
	}
    }

    /**
     * Returns the preferred size of the cell renderer component without ordering arrow.
     *
     * @return
     */
    private Dimension getPrefferedSizeWithOrdering() {
	final Dimension arrowSize = arrow.getMinimumSize();
	final int height = arrowSize.height + 6;
	return new Dimension(totalCellWidth, height);
    }

    /**
     * Returns the preferred size of the cell renderer component with ordering arrow.
     *
     * @return
     */
    private Dimension getPrefferedSizeWithoutOrdering() {
	final Dimension superDimensions = super.getPreferredSize();
	final int width = superDimensions.width + getButtonsWidth();
	final int buttonHeight = getMaximumButtonHeight();
	final int height = buttonHeight < superDimensions.height ? superDimensions.height : buttonHeight;
	return new Dimension(width, height);
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
}
