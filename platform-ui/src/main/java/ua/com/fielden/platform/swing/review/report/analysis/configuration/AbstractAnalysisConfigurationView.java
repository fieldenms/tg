package ua.com.fielden.platform.swing.review.report.analysis.configuration;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.report.analysis.view.AbstractAnalysisReview;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationView;
import ua.com.fielden.platform.swing.review.report.events.SelectionEvent;
import ua.com.fielden.platform.swing.review.report.interfaces.ISelectionEventListener;
import ua.com.fielden.platform.swing.review.wizard.development.AbstractWizardView;

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
 * @param <WT> - The type of {@link AbstractWizardView} that represent the analysis wizard.
 */
public abstract class AbstractAnalysisConfigurationView<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer, ADTM extends IAbstractAnalysisDomainTreeManager, LDT, VT extends AbstractAnalysisReview<T, CDTME, ADTM, LDT>, WT extends AbstractWizardView<T>> extends AbstractConfigurationView<VT, WT> {

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

    /**
     * Returns the entity centre that owns this analysis.
     *
     * @return
     */
    protected final AbstractEntityCentre<T, CDTME> getOwner() {
	return owner;
    }

    @SuppressWarnings("unchecked")
    @Override
    public AbstractAnalysisConfigurationModel<T, CDTME> getModel() {
	return (AbstractAnalysisConfigurationModel<T, CDTME>)super.getModel();
    }
}
