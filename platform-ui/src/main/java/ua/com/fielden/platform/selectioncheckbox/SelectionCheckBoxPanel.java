package ua.com.fielden.platform.selectioncheckbox;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.SortOrder;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.reportquery.IDistributedProperty;
import ua.com.fielden.platform.swing.review.OrderingArrow;
import ua.com.fielden.platform.types.Ordering;
import ua.com.fielden.platform.utils.Pair;

/**
 * The panel that holds the check boxes associated with some values. Please Note that this class creates check boxes and associates them with appropriate value and also adds them
 * to the container.
 *
 * @author oleh
 *
 * @param <TOKEN>
 */
public class SelectionCheckBoxPanel<TOKEN, PROPERTY extends IDistributedProperty> extends JPanel {
    private static final long serialVersionUID = 2253532295346085668L;

    /**
     * Holds the check boxes associated with {@link String} value
     */
    private final Map<TOKEN, Pair<CheckBox<TOKEN>, OrderingArrow>> availableItems = new HashMap<TOKEN, Pair<CheckBox<TOKEN>, OrderingArrow>>();
    /**
     * Previously selected ordering property.
     */
    private Ordering<TOKEN, PROPERTY> ordering;
    /**
     * Needed to handle ordering actions.
     */
    private PROPERTY currentProperty;

    private final IAction action;

    public static interface IAction {
	void action();
    }

    /**
     * Creates the check boxes and associates them with {@code availableItems}
     *
     * @param ordering
     */
    public SelectionCheckBoxPanel(final Ordering<TOKEN, PROPERTY> ordering, final TOKEN[] tokens, final IAction action) {
	super(new MigLayout("fill, insets 0", "[grow, fill]0[fill]")); //" + minimumOrderingArrowWidth + ":" +minimumOrderingArrowWidth + ":
	this.action = action;
	setOrdering(ordering);
	reloadTokens(tokens);
    }

    private void createItemListenerFor(final TOKEN item) {
	final Pair<CheckBox<TOKEN>, OrderingArrow> orderingCheckBox = availableItems.get(item);
	if (orderingCheckBox == null || orderingCheckBox.getKey() == null || orderingCheckBox.getValue() == null) {
	    return;
	}
	final OrderingArrow arrow = orderingCheckBox.getValue();
	orderingCheckBox.getKey().addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		final AbstractButton abstractButton = (AbstractButton) e.getSource();
		if (!abstractButton.getModel().isSelected()) {
		    arrow.setSortOrder(SortOrder.UNSORTED);
		    arrow.setVisible(false);
		    if (getOrdering() != null && item.equals(getOrdering().getToken()) && getCurrentProperty() != null && getCurrentProperty().equals(getOrdering().getProperty())) {
			setOrdering(null);
		    }
		} else {
		    arrow.setVisible(true);
		}
		if (action != null) {
		    action.action();
		}
	    }

	});
    }

    private void createMouseListenerFor(final TOKEN item) {
	final Pair<CheckBox<TOKEN>, OrderingArrow> orderingCheckBox = availableItems.get(item);
	if (orderingCheckBox == null || orderingCheckBox.getKey() == null || orderingCheckBox.getValue() == null) {
	    return;
	}
	final OrderingArrow arrow = orderingCheckBox.getValue();
	arrow.addMouseListener(new MouseAdapter() {

	    @Override
	    public void mouseClicked(final MouseEvent e) {
		if (!arrow.isEnabled()) {
		    return;
		}
		final SortOrder sortOrder = nextOrder(arrow.getSortOrder());
		if (getCurrentProperty() != null) {
		    if (sortOrder != SortOrder.UNSORTED) {
			setOrdering(new Ordering<TOKEN, PROPERTY>(getCurrentProperty(), item, sortOrder));
		    } else {
			setOrdering(null);
		    }
		}
		for (final Pair<CheckBox<TOKEN>, OrderingArrow> pair : availableItems.values()) {
		    pair.getValue().setSortOrder(SortOrder.UNSORTED);
		}
		arrow.setSortOrder(sortOrder);
		if (action != null) {
		    action.action();
		}
	    }

	    private SortOrder nextOrder(final SortOrder order) {
		switch (order) {
		case ASCENDING:
		    return SortOrder.DESCENDING;
		case DESCENDING:
		    return SortOrder.UNSORTED;
		case UNSORTED:
		    return SortOrder.ASCENDING;
		}
		return null;
	    }

	    @Override
	    public void mouseEntered(final MouseEvent e) {
		if (arrow.isEnabled()) {
		    arrow.setMouseOver(true);
		}
	    }

	    @Override
	    public void mouseExited(final MouseEvent e) {
		if (arrow.isEnabled()) {
		    arrow.setMouseOver(false);
		}
	    }

	});
    }

    private void setOrdering(final Ordering<TOKEN, PROPERTY> ordering) {
	this.ordering = ordering;
    }

    public Ordering<TOKEN, PROPERTY> getOrdering() {
	return ordering;
    }

    /**
     * Return the list of items those are associated with selected check boxes.
     *
     * @return
     */
    public List<TOKEN> getSelectedItem() {
	final List<TOKEN> selectedItems = new ArrayList<TOKEN>();
	for (final Pair<CheckBox<TOKEN>, OrderingArrow> item : availableItems.values()) {
	    final CheckBox<TOKEN> checkBox = item.getKey();
	    if ((checkBox != null) && (checkBox.isSelected())) {
		selectedItems.add(checkBox.getToken());
	    }
	}
	return selectedItems;
    }

    private void setCurrentProperty(final PROPERTY currentProperty) {
	this.currentProperty = currentProperty;
    }

    private PROPERTY getCurrentProperty() {
	return currentProperty;
    }

    public void reloadTokens(final TOKEN[] newTokens) {
	this.availableItems.clear();
	removeAll();
	for (int itemCounter = 0; itemCounter < newTokens.length; itemCounter++) {
	    final TOKEN item = newTokens[itemCounter];
	    final CheckBox<TOKEN> checkBox = new CheckBox<TOKEN>(item);
	    checkBox.setEnabled(getCurrentProperty() != null);
	    final OrderingArrow orderingArrow = new OrderingArrow();
	    orderingArrow.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	    orderingArrow.setOrder(1);
	    orderingArrow.setVisible(false);
	    orderingArrow.setDrawNumber(false);
	    this.availableItems.put(item, new Pair<CheckBox<TOKEN>, OrderingArrow>(checkBox, orderingArrow));
	    createMouseListenerFor(item);
	    createItemListenerFor(item);
	    add(checkBox);
	    if (itemCounter < newTokens.length - 1) {
		add(orderingArrow, "growx, wrap");
	    } else {
		add(orderingArrow, "growx");
	    }
	}

	invalidate();
	revalidate();
	repaint();
    }

    /**
     * Searches the check box that is associated with selectdItem and set it's selected property to true.
     *
     * @param selectedItems
     */
    public void setSelectedCheckBoxes(final List<TOKEN> selectedItems, final PROPERTY currentProperty) {
	setCurrentProperty(currentProperty);
	setEnabled(getCurrentProperty() != null);
	uncheckAllCheckBoxes();
	if (selectedItems == null) {
	    return;
	}
	for (final TOKEN functionToken : selectedItems) {
	    final Pair<CheckBox<TOKEN>, OrderingArrow> orderedCheckBox = availableItems.get(functionToken);
	    if (orderedCheckBox != null) {
		orderedCheckBox.getKey().setSelected(true);
		orderedCheckBox.getValue().setVisible(true);
		if (getOrdering() != null && currentProperty != null && currentProperty.equals(getOrdering().getProperty()) && functionToken.equals(getOrdering().getToken())) {
		    orderedCheckBox.getValue().setSortOrder(getOrdering().getSortOrder());
		}
	    }
	}
    }

    private void uncheckAllCheckBoxes() {
	for (final Pair<CheckBox<TOKEN>, OrderingArrow> orderingCheckBox : availableItems.values()) {
	    orderingCheckBox.getKey().setSelected(false);
	    orderingCheckBox.getValue().setSortOrder(SortOrder.UNSORTED);
	    orderingCheckBox.getValue().setVisible(false);
	}
    }

    @Override
    public void setEnabled(final boolean enabled) {
	super.setEnabled(enabled);
	for (final Pair<CheckBox<TOKEN>, OrderingArrow> orderingCheckBox : availableItems.values()) {
	    orderingCheckBox.getKey().setEnabled(enabled);
	    orderingCheckBox.getValue().setEnabled(enabled);
	}
    }

    public static class CheckBox<T> extends JCheckBox {
	private static final long serialVersionUID = 7117583477015151656L;

	private final T token;

	public CheckBox(final T token) {
	    super(token.toString());
	    this.token = token;
	}

	public T getToken() {
	    return token;
	}
    }

    protected Map<TOKEN, Pair<CheckBox<TOKEN>, OrderingArrow>> getAvailableItems() {
	return availableItems;
    }
}
