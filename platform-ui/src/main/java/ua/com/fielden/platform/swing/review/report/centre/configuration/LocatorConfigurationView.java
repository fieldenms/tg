package ua.com.fielden.platform.swing.review.report.centre.configuration;

import javax.swing.JOptionPane;

import ua.com.fielden.platform.domaintree.ILocatorManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.report.centre.SingleAnalysisEntityLocator;
import ua.com.fielden.platform.swing.review.report.centre.wizard.EntityCentreWizard;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationView;
import ua.com.fielden.platform.swing.review.report.events.LocatorEvent;
import ua.com.fielden.platform.swing.review.report.events.ReviewEvent;
import ua.com.fielden.platform.swing.review.report.events.WizardEvent;
import ua.com.fielden.platform.swing.review.report.interfaces.ILocatorEventListener;
import ua.com.fielden.platform.swing.review.report.interfaces.IReviewEventListener;
import ua.com.fielden.platform.swing.review.report.interfaces.IWizardEventListener;

public class LocatorConfigurationView<T extends AbstractEntity<?>, R extends AbstractEntity<?>> extends AbstractConfigurationView<SingleAnalysisEntityLocator<T>, EntityCentreWizard<T>> {

    private static final long serialVersionUID = 7422543091832971730L;

    private final boolean isMultipleSelection;

    public LocatorConfigurationView(final LocatorConfigurationModel<T, R> model, final BlockingIndefiniteProgressLayer progressLayer, final boolean isMultipleSelection) {
	super(model, progressLayer);
	this.isMultipleSelection = isMultipleSelection;
    }

    @Override
    public String getInfo() {
	return "Locator configuration panel.";
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

    @SuppressWarnings("unchecked")
    @Override
    public final LocatorConfigurationModel<T, R> getModel() {
	return (LocatorConfigurationModel<T, R>)super.getModel();
    }

    @Override
    protected final SingleAnalysisEntityLocator<T> initConfigurableView(final SingleAnalysisEntityLocator<T> configurableView) {
	final SingleAnalysisEntityLocator<T> entityLocator = new SingleAnalysisEntityLocator<T>(getModel().createEntityCentreModel(), getProgressLayer(), isMultipleSelection);
	entityLocator.addLocatorEventListener(new ILocatorEventListener() {

	    @Override
	    public void locatorActionPerformed(final LocatorEvent event) {
		fireLocatorEvent(event);
	    }
	});
	entityLocator.addReviewEventListener(createLocatorEventListener());
	return super.initConfigurableView(entityLocator);
    }

    @Override
    protected final EntityCentreWizard<T> initWizardView(final EntityCentreWizard<T> wizardView) {
	final EntityCentreWizard<T> newWizardView = new EntityCentreWizard<T>(getModel().createDomainTreeEditorModel(), getProgressLayer());
	newWizardView.addWizardEventListener(createCentreWizardListener());
	return super.initWizardView(newWizardView);
    }

    private void fireLocatorEvent(final LocatorEvent event){
	for(final ILocatorEventListener listener : listenerList.getListeners(ILocatorEventListener.class)){
	    listener.locatorActionPerformed(event);
	}
    }

    /**
     * Returns specific {@link IReviewEventListener} for the locator.
     *
     * @return
     */
    private IReviewEventListener createLocatorEventListener() {
	return new IReviewEventListener() {

	    @Override
	    public boolean configureActionPerformed(final ReviewEvent e) {
		switch (e.getReviewAction()) {
		case CONFIGURE:
		    getModel().locatorManager.freezeLocatorManager(getModel().rootType, getModel().name);
		    break;
		}
		return true;
	    }
	};
    }

    /**
     * Provides the specific entity locators's wizard listener.
     *
     * @return
     */
    private IWizardEventListener createCentreWizardListener(){
	return new IWizardEventListener() {

	    @Override
	    public boolean wizardActionPerformed(final WizardEvent e) {
		final Class<R> root = getModel().rootType;
		final String name = getModel().name;
		final ILocatorManager locatorManager = getModel().locatorManager;

		// TODO The logic should be revised after ILocatorManager enhancements!
		final boolean isFreezed = ILocatorManager.Phase.FREEZED_EDITING_PHASE == locatorManager.phaseAndTypeOfLocatorManager(root, name).getKey();

		switch (e.getWizardAction()) {
		case PRE_CANCEL:
		    if(!isFreezed){
			JOptionPane.showMessageDialog(LocatorConfigurationView.this, "This locator's wizard can not be canceled!", "Warning", JOptionPane.WARNING_MESSAGE);
			return false;
		    }
		    break;
		case CANCEL:
		    if(isFreezed){
			locatorManager.discardLocatorManager(root, name);
		    }
		    break;
		case BUILD:
		    if(isFreezed){
			locatorManager.acceptLocatorManager(root, name);
		    }
		    break;
		}
		return true;
	    }
	};
    }
}
