package ua.com.fielden.platform.swing.review.report.centre.configuration;

import javax.swing.JOptionPane;

import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.model.ICloseGuard;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;
import ua.com.fielden.platform.swing.review.report.centre.wizard.EntityCentreWizard;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationView;
import ua.com.fielden.platform.swing.review.report.events.ReviewEvent;
import ua.com.fielden.platform.swing.review.report.events.WizardEvent;
import ua.com.fielden.platform.swing.review.report.interfaces.IReviewEventListener;
import ua.com.fielden.platform.swing.review.report.interfaces.IWizardEventListener;

public class CentreConfigurationView<T extends AbstractEntity<?>, C extends AbstractEntityCentre<T, ICentreDomainTreeManagerAndEnhancer>> extends AbstractConfigurationView<C, EntityCentreWizard<T>> {

    private static final long serialVersionUID = -2895046742734467746L;

    public CentreConfigurationView(final CentreConfigurationModel<T> model, final BlockingIndefiniteProgressLayer progressLayer) {
	super(model, progressLayer);
    }

    @Override
    public String getInfo() {
	return "Centre configuration panel.";
    }

    @SuppressWarnings("unchecked")
    @Override
    public final CentreConfigurationModel<T> getModel() {
	return (CentreConfigurationModel<T>)super.getModel();
    }

    @Override
    public final ICloseGuard canClose() {
	return getModel().canClose() ? null : this;
    }

    @Override
    public final void close() {
	getModel().close();
    }

    @Override
    protected C initConfigurableView(final C configurableView) {
	if (configurableView != null) {
	    configurableView.addReviewEventListener(createCentreEventListener());
	}
	return super.initConfigurableView(configurableView);
    }

    @Override
    protected final EntityCentreWizard<T> initWizardView(final EntityCentreWizard<T> wizardView) {
	final EntityCentreWizard<T> newWizardView = new EntityCentreWizard<T>(getModel().createDomainTreeEditorModel(), getProgressLayer());
	newWizardView.addWizardEventListener(createCentreWizardListener());
	return super.initWizardView(newWizardView);
    }

    /**
     * Returns the {@link IReviewEventListener} that handles entity centre's configure action.
     * 
     * @return
     */
    private IReviewEventListener createCentreEventListener() {
	return new IReviewEventListener() {

	    @Override
	    public boolean configureActionPerformed(final ReviewEvent e) {
		switch (e.getReviewAction()) {
		case CONFIGURE:
		    getModel().gdtm.freezeEntityCentreManager(getModel().entityType, getModel().name);
		    break;
		}
		return true;
	    }
	};
    }

    /**
     * Provides the specific entity centre's wizard listener.
     * 
     * @return
     */
    private IWizardEventListener createCentreWizardListener(){
	return new IWizardEventListener() {

	    @Override
	    public boolean wizardActionPerformed(final WizardEvent e) {
		final Class<T> root = getModel().entityType;
		final String name = getModel().name;
		final IGlobalDomainTreeManager gdtm = getModel().gdtm;
		switch (e.getWizardAction()) {
		case PRE_CANCEL:
		    if(!gdtm.isFreezedEntityCentreManager(root, name)){
			JOptionPane.showMessageDialog(CentreConfigurationView.this, "This wizard can not be canceled!", "Warning", JOptionPane.WARNING_MESSAGE);
			return false;
		    }
		    break;
		case CANCEL:
		    if(gdtm.isFreezedEntityCentreManager(root, name)){
			gdtm.discardEntityCentreManager(root, name);
		    }
		    break;
		case BUILD:
		    if(gdtm.isFreezedEntityCentreManager(root, name)){
			gdtm.saveEntityCentreManager(root, name);
		    }
		    break;
		}
		return true;
	    }
	};
    }
}
