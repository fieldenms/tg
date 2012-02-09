package ua.com.fielden.platform.swing.review.report.centre.configuration;

import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.report.centre.SingleAnalysisEntityLocator;
import ua.com.fielden.platform.swing.review.report.events.LocatorEvent;
import ua.com.fielden.platform.swing.review.report.interfaces.ILocatorEventListener;

public class LocatorConfigurationView<T extends AbstractEntity, R extends AbstractEntity> extends AbstractCentreConfigurationView<T, ILocatorDomainTreeManager, SingleAnalysisEntityLocator<T>> {

    private static final long serialVersionUID = 7422543091832971730L;

    private final boolean isMultipleSelection;

    public LocatorConfigurationView(final LocatorConfigurationModel<T, R> model, final BlockingIndefiniteProgressLayer progressLayer, final boolean isMultipleSelection) {
	super(model, progressLayer);
	this.isMultipleSelection = isMultipleSelection;
    }

    @SuppressWarnings("unchecked")
    @Override
    public LocatorConfigurationModel<T, R> getModel() {
	return (LocatorConfigurationModel<T, R>)super.getModel();
    }

    public void addLocatorEventListener(final ILocatorEventListener l){
	listenerList.add(ILocatorEventListener.class, l);
    }

    public void removeLocatorEventListener(final ILocatorEventListener l){
	listenerList.remove(ILocatorEventListener.class, l);
    }

    public boolean isMultipleSelection() {
	return isMultipleSelection;
    }

    @Override
    protected SingleAnalysisEntityLocator<T> createConfigurableView() {
	final SingleAnalysisEntityLocator<T> entityLocator = new SingleAnalysisEntityLocator<T>(getModel().createEntityCentreModel(), getProgressLayer(), isMultipleSelection);
	entityLocator.addLocatorEventListener(new ILocatorEventListener() {

	    @Override
	    public void locatorActionPerformed(final LocatorEvent event) {
		fireLocatorEvent(event);
	    }
	});
	return entityLocator;
    }

    private void fireLocatorEvent(final LocatorEvent event){
	for(final ILocatorEventListener listener : listenerList.getListeners(ILocatorEventListener.class)){
	    listener.locatorActionPerformed(event);
	}
    }
}
