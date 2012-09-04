package ua.com.fielden.platform.swing.review.report.analysis.view;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.EventListenerList;

import ua.com.fielden.platform.domaintree.centre.IOrderingManager;
import ua.com.fielden.platform.domaintree.centre.IOrderingManager.IPropertyOrderingListener;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.checkboxlist.ListSortingModel;
import ua.com.fielden.platform.swing.checkboxlist.SorterChangedEvent;
import ua.com.fielden.platform.swing.checkboxlist.SorterEventListener;
import ua.com.fielden.platform.utils.Pair;

/**
 * This {@link ListSortingModel} wraps {@link IOrderingManager} and {@link IOrderingRepresentation} instances.
 *
 * @author TG Team
 *
 * @param <T>
 */
public class DomainTreeListSortingModel<T extends AbstractEntity<?>> implements ListSortingModel<String> {

    /**
     * The entity type for wrapped {@link IOrderingManager} and {@link IOrderingRepresentation} instances.
     */
    private final Class<T> root;

    /**
     * The wrapped {@link IOrderingManager} instance.
     */
    private final IOrderingManager orderingManager;

    /**
     * The wrapped {@link IOrderingRepresentation} instance.
     */
    private final IOrderingRepresentation orderingRepresentation;

    /**
     * This listener list allows to integrate {@link IOrderingManager} with this list sorting model.
     */
    private final EventListenerList listeners = new EventListenerList();

    /**
     * The default listener that listens the ordering model changes.
     */
    private final IPropertyOrderingListener defaultPropertyOrderingListener;

    /**
     * Initialises this {@link DomainTreeListSortingModel} with {@link IOrderingManager} and {@link IOrderingRepresentation} to wrap.
     *
     * @param root
     * @param orderingManager
     * @param orderingRepresentation
     */
    public DomainTreeListSortingModel(final Class<T> root, final IOrderingManager orderingManager, final IOrderingRepresentation orderingRepresentation){
	this.root = root;
	this.orderingManager = orderingManager;
	this.orderingRepresentation = orderingRepresentation;
	this.defaultPropertyOrderingListener = new IPropertyOrderingListener() {

	    @Override
	    public void propertyStateChanged(final Class<?> root, final String property, final List<Pair<String, Ordering>> newOrderedProperties, final List<Pair<String, Ordering>> oldState) {
		fireSortingModelChanged(new SorterChangedEvent<String>(DomainTreeListSortingModel.this, newOrderedProperties, oldState));
	    }
	};
	orderingManager.addPropertyOrderingListener(defaultPropertyOrderingListener);
    }

    @Override
    public void toggleSorter(final String item) {
	orderingManager.toggleOrdering(root, item);
    }

    @Override
    public List<Pair<String, Ordering>> getSortObjects() {
	return new ArrayList<Pair<String,Ordering>>(orderingManager.orderedProperties(root));
    }

    @Override
    public void setItemUnsortable(final String item) {
	orderingRepresentation.disableOrderingImmutably(root, item);
    }

    @Override
    public boolean isSortable(final String item) {
	return !orderingRepresentation.isOrderingDisabledImmutably(root, item);
    }

    @Override
    public void addSorterEventListener(final SorterEventListener<String> listener) {
	listeners.add(SorterEventListener.class, listener);
    }

    @Override
    public void removeSorterEventListener(final SorterEventListener<String> listener) {
	listeners.remove(SorterEventListener.class, listener);
    }

    @Override
    public void toggleSorterSingle(final String item) {
	orderingManager.removePropertyOrderingListener(defaultPropertyOrderingListener);
	resetAllSortOrderExcept(item);
	orderingManager.addPropertyOrderingListener(defaultPropertyOrderingListener);
	toggleSorter(item);
    }

    /**
     * Fires the sorting model change event.
     *
     * @param sorterEvent
     */
    @SuppressWarnings("unchecked")
    private void fireSortingModelChanged(final SorterChangedEvent<String> sorterEvent){
	for(final SorterEventListener<String> listener : listeners.getListeners(SorterEventListener.class)){
	    listener.valueChanged(sorterEvent);
	}
    }

    /**
     * Rests the sort order for the specified pair of sorting value and it's ordering.
     *
     * @param sortObject
     */
    private void resetSortOrder(final String item, final Ordering ordering) {
	toggleSorter(item);
	if(ordering == Ordering.ASCENDING){
	    toggleSorter(item);
	}
    }

    /**
     * Resets all sort orders except the specified one.
     *
     * @param item
     */
    private void resetAllSortOrderExcept(final String item){
	for(final Pair<String, Ordering> sortOrder : getSortObjects()){
	    if(!sortOrder.getKey().equals(item)){
		resetSortOrder(sortOrder.getKey(), sortOrder.getValue());
	    }
	}
    }
}
