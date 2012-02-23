package ua.com.fielden.platform.swing.components.bind.development;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.IBindingEntity;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.utils.PropertyChangeSupportEx.PropertyChangeOrIncorrectAttemptListener;

/**
 * This class adds change listeners to property hierarchy of some entity using methods like
 * {@link #addListenersToHierarchy(AbstractEntity, List, PropertyChangeListener, PropertyChangeListener)} . These listeners on hierarchy change are automatically detached from old
 * hierarchy and attached to new one.
 * 
 * @author Yura
 */
public class HierarchicalPropertyChangeListener implements PropertyChangeOrIncorrectAttemptListener {

    private final IBindingEntity entity;

    private final List<String> propertyNames;

    private HierarchicalPropertyChangeListener nextListener = null;

    private final PropertyChangeListener propertyChangeListener;

    private final PropertyChangeListener validationResultsChangeListener;

    /**
     * Creates instance of {@link HierarchicalPropertyChangeListener} and initializes properties.
     * 
     * @param entity
     * @param propertyNames
     * @param propertyChangeListener
     * @param validationResultsChangeListener
     */
    private HierarchicalPropertyChangeListener(final IBindingEntity entity, final List<String> propertyNames, final PropertyChangeListener propertyChangeListener, final PropertyChangeListener validationResultsChangeListener) {
	this.entity = entity;
	this.propertyNames = propertyNames;
	this.propertyChangeListener = propertyChangeListener;
	this.validationResultsChangeListener = validationResultsChangeListener;
    }

    /**
     * Returns next {@link HierarchicalPropertyChangeListener} from a list. Could be null if this is the last listener in a list
     * 
     * @see #addListenersToPropertyHierarchy(IBindingEntity, String, PropertyChangeListener, PropertyChangeListener)
     * @return
     */
    private HierarchicalPropertyChangeListener getNextListener() {
	return nextListener;
    }

    /**
     * Sets next {@link HierarchicalPropertyChangeListener}.
     * 
     * @see #addListenersToPropertyHierarchy(IBindingEntity, String, PropertyChangeListener, PropertyChangeListener)
     * @param nextListener
     */
    private void setNextListener(final HierarchicalPropertyChangeListener nextListener) {
	this.nextListener = nextListener;
    }

    /**
     * Returns entity, which this listener was added to
     * 
     * @return
     */
    private IBindingEntity getEntity() {
	return entity;
    }

    /**
     * Returns list of property names, beginning from property of this entity.
     * 
     * @return
     */
    private List<String> getPropertyNames() {
	return propertyNames;
    }

    /**
     * Returns {@link PropertyChangeListener} that is associated with this {@link HierarchicalPropertyChangeListener}. Could be null.
     * 
     * @return
     */
    private PropertyChangeListener getPropertyChangeListener() {
	return propertyChangeListener;
    }

    /**
     * Returns {@link PropertyChangeListener} that is added using {@link MetaProperty#addValidationResultsChangeListener(PropertyChangeListener)} method, and is associated with
     * this {@link HierarchicalPropertyChangeListener}. Could be null.
     * 
     * @return
     */
    private PropertyChangeListener getValidationResultsChangeListener() {
	return validationResultsChangeListener;
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
	if (evt.getOldValue() != null && getNextListener() != null) {
	    // if old hierarchy existed (was not null, and this is not the last property in hierarchy)
	    // then detaching listeners from old hierarchy
	    getNextListener().removeFromHierarchy();
	    nextListener = null;
	}
	if (evt.getNewValue() != null && getPropertyNames().size() > 1) {
	    // if new hierarchy exists (is not null), and this is not the last property in hierarchy)
	    // then attaching listeners to new hierarchy
	    final AbstractEntity<?> newEntity = (AbstractEntity<?>) evt.getNewValue();
	    nextListener = addListenersToHierarchy(newEntity, removeFromHead(getPropertyNames()), getPropertyChangeListener(), getValidationResultsChangeListener());
	}

	if (getPropertyChangeListener() != null) {
	    // if there is some property-change listener associated with this listener
	    // then we should get old and new values and fire PropertyChangeEvent on it
	    Object oldBottomValue, newBottomValue;
	    if (getPropertyNames().size() == 1) {
		// if this is the last property in hierarchy, then
		oldBottomValue = evt.getOldValue();
		newBottomValue = evt.getNewValue();
	    } else {
		// getting dot-notated property string
		String dotNotatedPropertyName = "";
		for (int i = 1; i < getPropertyNames().size(); i++) {
		    dotNotatedPropertyName += getPropertyNames().get(i) + ".";
		}
		dotNotatedPropertyName = dotNotatedPropertyName.substring(0, dotNotatedPropertyName.length() - 1);

		oldBottomValue = evt.getOldValue() == null ? null : ((AbstractEntity<?>) evt.getOldValue()).get(dotNotatedPropertyName);
		newBottomValue = evt.getNewValue() == null ? null : ((AbstractEntity<?>) evt.getNewValue()).get(dotNotatedPropertyName);
	    }
	    getPropertyChangeListener().propertyChange(new PropertyChangeEvent(evt.getSource(), evt.getPropertyName(), oldBottomValue, newBottomValue));
	}
    }

    /**
     * Recursively removes this listener and following listeners from entities they were added to. Also removes all validation results change listeners.
     */
    private void removeFromHierarchy() {
	getEntity().removePropertyChangeListener(getPropertyNames().get(0), this);
	if (getValidationResultsChangeListener() != null && getEntity().getProperty(getPropertyNames().get(0)) != null) {
	    getEntity().getProperty(getPropertyNames().get(0)).removeValidationResultsChangeListener(validationResultsChangeListener);
	}
	if (getNextListener() != null) {
	    getNextListener().removeFromHierarchy();
	}
    }

    /**
     * Recursively creates and adds {@link HierarchicalPropertyChangeListener}s to property hierarchy specified by <code>entity</code> and <code>dotNotation</code> parameters. This
     * process actually forms a unidirectional list of {@link HierarchicalPropertyChangeListener}s (i.e. if <code>dotNotation</code> is "property1.property2.property3" then
     * {@link HierarchicalPropertyChangeListener}s would be added to entity->property1, property1->property2, property2->property3 and each listener would have reference to next
     * listener). In case when hierarchy is modified listeners added to old hierarchy would be detached, and new listeners would be created and attached to new hierarchy.<br>
     * <br>
     * Returns reference to top listener in listeners list.
     * 
     * @param entity
     * @param dotNotation
     * @param propertyChangeListener
     * @param validationResultsChangeListener
     * @return
     */
    public static HierarchicalPropertyChangeListener addListenersToPropertyHierarchy(final IBindingEntity entity, final String dotNotation, final PropertyChangeListener propertyChangeListener, final PropertyChangeListener validationResultsChangeListener) {
	return addListenersToHierarchy(entity, Arrays.asList(dotNotation.split("\\.")), propertyChangeListener, validationResultsChangeListener);
    }

    /**
     * @see #addListenersToPropertyHierarchy(IBindingEntity, String, PropertyChangeListener, PropertyChangeListener)
     * @param entity
     * @param dotNotation
     * @param propertyChangeListener
     * @return
     */
    public static HierarchicalPropertyChangeListener addPropertyListenerToPropertyHierarchy(final IBindingEntity entity, final String dotNotation, final PropertyChangeListener propertyChangeListener) {
	return addListenersToHierarchy(entity, Arrays.asList(dotNotation.split("\\.")), propertyChangeListener, null);
    }

    /**
     * @see #addListenersToPropertyHierarchy(IBindingEntity, String, PropertyChangeListener, PropertyChangeListener)
     * @param entity
     * @param dotNotation
     * @param validationResultsChangeListener
     * @return
     */
    public static HierarchicalPropertyChangeListener addValidationListenerToPropertyHierarchy(final IBindingEntity entity, final String dotNotation, final PropertyChangeListener validationResultsChangeListener) {
	return addListenersToHierarchy(entity, Arrays.asList(dotNotation.split("\\.")), null, validationResultsChangeListener);
    }

    /**
     * @see #addListenersToPropertyHierarchy(IBindingEntity, String, PropertyChangeListener, PropertyChangeListener)
     * @param entity
     * @param propertyNames
     * @param propertyChangeListener
     * @param validationResultsChangeListener
     * @return
     */
    private static HierarchicalPropertyChangeListener addListenersToHierarchy(final IBindingEntity entity, final List<String> propertyNames, final PropertyChangeListener propertyChangeListener, final PropertyChangeListener validationResultsChangeListener) {
	IBindingEntity nextEntity = entity;
	HierarchicalPropertyChangeListener previousListener = null, topListener = null;
	// iterating over property names, creating listeners and adding them to hierarchy
	for (int i = 0; i < propertyNames.size(); i++) {
	    if (nextEntity != null) {
		// getting list of property names to correctly initialize HierarchicalPropertyChangeListener (actually removing first element from list)
		final List<String> trimmedPropertyNames = previousListener == null ? propertyNames : removeFromHead(previousListener.getPropertyNames());
		final HierarchicalPropertyChangeListener newListener = new HierarchicalPropertyChangeListener(nextEntity, trimmedPropertyNames, propertyChangeListener, validationResultsChangeListener);
		// adding created listener to part of property hierarchy
		nextEntity.addPropertyChangeListener(propertyNames.get(i), newListener);
		if (validationResultsChangeListener != null && nextEntity.getProperty(trimmedPropertyNames.get(0)) != null) {
		    nextEntity.getProperty(trimmedPropertyNames.get(0)).addValidationResultsChangeListener(validationResultsChangeListener);
		}

		if (previousListener == null) {
		    // storing top listener to return it later
		    topListener = newListener;
		} else {
		    // initializing reference from previous listener to this one
		    previousListener.setNextListener(newListener);
		}
		previousListener = newListener;

		if (i != propertyNames.size() - 1) {
		    nextEntity = (IBindingEntity) nextEntity.get(propertyNames.get(i));
		}
	    }
	}
	return topListener;
    }

    /**
     * Removes all previously added {@link HierarchicalPropertyChangeListener}s and validation results change listeners from hierarchy.
     * 
     * @param topListener
     */
    public static void removeListenersFromHierarchy(final HierarchicalPropertyChangeListener topListener) {
	// removing all listeners from hierarchy
	topListener.removeFromHierarchy();

	// removing passed listener from its entity
	final IBindingEntity entity = topListener.getEntity();
	final String propertyName = topListener.getPropertyNames().get(topListener.getPropertyNames().size() - 1);
	entity.removePropertyChangeListener(propertyName, topListener);
    }

    /**
     * Utility method that creates copy of a passed list, removes first element and returns it.
     * 
     * @param <T>
     * @param list
     * @return
     */
    public static <T> List<T> removeFromHead(final List<T> list) {
	final List<T> newList = new ArrayList<T>(list.size());
	newList.addAll(list);

	newList.remove(0);
	return newList;
    }

}
