package ua.com.fielden.platform.swing.review.report.analysis.view;

import javax.swing.event.EventListenerList;

import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager.IUsageManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager.IUsageManager.IPropertyUsageListener;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.checkboxlist.ListCheckingEvent;
import ua.com.fielden.platform.swing.checkboxlist.ListCheckingListener;
import ua.com.fielden.platform.swing.checkboxlist.ListCheckingModel;

import com.jidesoft.swing.CheckBoxList;

/**
 * This {@link ListCheckingModel} wraps {@link IUsageManager} and can be used as a checking model for {@link CheckBoxList}.
 *
 * @author TG Team
 *
 */
public class DomainTreeListCheckingModel<T extends AbstractEntity<?>> implements ListCheckingModel<String> {

    /**
     * Listener list that allows to integrate wrapped usage manager with list checking model listener.
     */
    private final EventListenerList listeners = new EventListenerList();

    /**
     * Wrapped {@link IUsageManager} instance.
     */
    private final IUsageManager usageManager;

    /**
     * The entity type for wrapped {@link IUsageManager} instance.
     */
    private final Class<T> root;

    /**
     * Initiates this {@link DomainTreeListCheckingModel} and wraps the specified {@link IUsageManager} instance.
     *
     * @param usageManager
     */
    public DomainTreeListCheckingModel(final Class<T> root, final IUsageManager usageManager){
	this.root = root;
	this.usageManager = usageManager;
	this.usageManager.addPropertyUsageListener(new IPropertyUsageListener() {

	    @Override
	    public void propertyStateChanged(final Class<?> root, final String property, final Boolean hasBeenUsed, final Boolean oldState) {
		fireCheckingModelChanged(new ListCheckingEvent<String>(this, property, oldState, hasBeenUsed));
	    }
	});
    }

    @Override
    public void checkValue(final String value, final boolean check) {
	usageManager.use(root, value, check);
    }

    @Override
    public String[] getCheckingValues(final String[] values) {
	return usageManager.usedProperties(root).toArray(new String[0]);
    }

    @Override
    public Object[] getCheckingValues() {
	return usageManager.usedProperties(root).toArray();
    }

    @Override
    public boolean isValueChecked(final String value) {
	return usageManager.isUsed(root, value);
    }

    @Override
    public void toggleCheckingValue(final String value) {
	if(isValueChecked(value)){
	    checkValue(value, false);
	}else{
	    checkValue(value, true);
	}
    }

    @Override
    public void addListCheckingListener(final ListCheckingListener<String> listener) {
	listeners.add(ListCheckingListener.class, listener);
    }

    @Override
    public void removeListCheckingListener(final ListCheckingListener<String> listener) {
	listeners.remove(ListCheckingListener.class, listener);
    }

    @SuppressWarnings("unchecked")
    private void fireCheckingModelChanged(final ListCheckingEvent<String> checkingEvent){
	for(final ListCheckingListener<String> listener : listeners.getListeners(ListCheckingListener.class)){
	    listener.valueChanged(checkingEvent);
	}
    }
}
