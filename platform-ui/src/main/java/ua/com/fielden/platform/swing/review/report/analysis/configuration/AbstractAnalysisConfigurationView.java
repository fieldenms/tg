package ua.com.fielden.platform.swing.review.report.analysis.configuration;

import javax.swing.JOptionPane;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.report.analysis.view.AbstractAnalysisReview;
import ua.com.fielden.platform.swing.review.report.analysis.wizard.AnalysisWizardView;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationView;
import ua.com.fielden.platform.swing.review.report.events.ReviewEvent;
import ua.com.fielden.platform.swing.review.report.events.SelectionEvent;
import ua.com.fielden.platform.swing.review.report.events.WizardEvent;
import ua.com.fielden.platform.swing.review.report.interfaces.IReviewEventListener;
import ua.com.fielden.platform.swing.review.report.interfaces.ISelectionEventListener;
import ua.com.fielden.platform.swing.review.report.interfaces.IWizardEventListener;

/**
 * The base class for all type of analysis.
 *
 * @author TG Team
 *
 * @param <T> - The entity type for which this analysis was created.
 * @param <CDTME> - The type of {@link ICentreDomainTreeManagerAndEnhancer} that represent the centre/locator that owns this analysis.
 * @param <ADTM> - The type of {@link IAbstractAnalysisDomainTreeManager} that holds information for this analysis.
 * @param <LDT> - The type of data that this analysis returns after it's execution.
 * @param <VT> - The type of {@link AbstractAnalysisReview} that represent the analysis view.
 */
public abstract class AbstractAnalysisConfigurationView<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer, ADTM extends IAbstractAnalysisDomainTreeManager, LDT, VT extends AbstractAnalysisReview<T, CDTME, ADTM, LDT>> extends AbstractConfigurationView<VT, AnalysisWizardView<T, CDTME>> {

    private static final long serialVersionUID = -7493238859906828458L;

    /**
     * The entity centre that owns this analysis.
     */
    private final AbstractEntityCentre<T, CDTME> owner;


    public AbstractAnalysisConfigurationView(final AbstractAnalysisConfigurationModel<T, CDTME> model, final AbstractEntityCentre<T, CDTME> owner, final BlockingIndefiniteProgressLayer progressLayer) {
	super(model, progressLayer);
	this.owner = owner;
	addSelectionEventListener(createSelectionListener());
	owner.getPageHolderManager().addPageHolder(getModel().getPageHolder());
    }

    @SuppressWarnings("unchecked")
    @Override
    public AbstractAnalysisConfigurationModel<T, CDTME> getModel() {
	return (AbstractAnalysisConfigurationModel<T, CDTME>)super.getModel();
    }

    /**
     * Returns the entity centre that owns this analysis.
     *
     * @return
     */
    protected final AbstractEntityCentre<T, CDTME> getOwner() {
	return owner;
    }

    @Override
    protected VT initConfigurableView(final VT configurableView) {
	if(configurableView != null){
	    configurableView.addReviewEventListener(createAnalysisReviewListener());
	}
	return super.initConfigurableView(configurableView);
    }

    /**
     * Returns custom {@link IReviewEventListener} for the analysis view.
     * 
     * @return
     */
    private IReviewEventListener createAnalysisReviewListener() {
	return new IReviewEventListener() {

	    @Override
	    public boolean configureActionPerformed(final ReviewEvent e) {
		switch (e.getReviewAction()) {
		case CONFIGURE:
		    final ICentreDomainTreeManager cdtm = getModel().getCriteria().getCentreDomainTreeMangerAndEnhancer();
		    final String name = getModel().getName();
		    cdtm.freezeAnalysisManager(name);
		    break;
		}
		return true;
	    }
	};
    }

    @Override
    protected AnalysisWizardView<T, CDTME> initWizardView(final AnalysisWizardView<T, CDTME> wizardView) {
	if(wizardView != null){
	    wizardView.addWizardEventListener(createAnalysisWizardListener());
	}
	return super.initWizardView(wizardView);
    }

    /**
     * Returns custom {@link IWizardEventListener} for the analysis view.
     * 
     * @return
     */
    private IWizardEventListener createAnalysisWizardListener() {
	return new IWizardEventListener() {

	    @Override
	    public boolean wizardActionPerformed(final WizardEvent e) {
		final ICentreDomainTreeManager cdtm = getModel().getCriteria().getCentreDomainTreeMangerAndEnhancer();
		final String name = getModel().getName();
		switch (e.getWizardAction()) {
		case PRE_CANCEL:
		    if(!cdtm.isFreezedAnalysisManager(name)){
			JOptionPane.showMessageDialog(AbstractAnalysisConfigurationView.this, "This analysis wizard can not be canceled!", "Warning", JOptionPane.WARNING_MESSAGE);
			return false;
		    }
		    break;
		case CANCEL:
		    if(cdtm.isFreezedAnalysisManager(name)){
			cdtm.discardAnalysisManager(name);
		    }
		    break;
		case BUILD:
		    if(cdtm.isFreezedAnalysisManager(name)){
			cdtm.acceptAnalysisManager(name);
		    }
		    break;
		}
		return true;
	    }
	};
    }

    private ISelectionEventListener createSelectionListener() {
	return new ISelectionEventListener() {

	    @Override
	    public void viewWasSelected(final SelectionEvent event) {
		switch(getModel().getMode()){
		case REPORT : getPreviousView().select(); break;
		case WIZARD : getPreviousWizard().select();break;
		}
		owner.getPageHolderManager().selectPageHolder(getModel().getPageHolder());
	    }
	};
    }
}
