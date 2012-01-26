package ua.com.fielden.platform.swing.review.report.analysis.configuration;

import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.report.analysis.view.AbstractAnalysisReview;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationView;
import ua.com.fielden.platform.swing.review.report.events.SelectionEvent;
import ua.com.fielden.platform.swing.review.report.interfaces.ISelectionEventListener;
import ua.com.fielden.platform.swing.review.wizard.development.AbstractWizardView;

public abstract class AbstractAnalysisConfigurationView<T extends AbstractEntity, ADTM extends IAbstractAnalysisDomainTreeManager, LDT, VT extends AbstractAnalysisReview<T, ADTM, LDT>, WT extends AbstractWizardView<T>> extends AbstractConfigurationView<VT, WT> {

    private static final long serialVersionUID = -7493238859906828458L;

    /**
     * Specifies the analysis name
     */
    private final String analysisName;

    /**
     * The entity centre that owns this analysis.
     */
    private final AbstractEntityCentre<T> owner;


    public AbstractAnalysisConfigurationView(final String analysisName, final AbstractAnalysisConfigurationModel model, final AbstractEntityCentre<T> owner, final BlockingIndefiniteProgressLayer progressLayer) {
	super(model, progressLayer);
	this.analysisName = analysisName;
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
    protected final AbstractEntityCentre<T> getOwner() {
	return owner;
    }

    /**
     * Returns the analysis name.
     * 
     * @return
     */
    public final String getAnalysisName() {
	return analysisName;
    }

    @Override
    public AbstractAnalysisConfigurationModel getModel() {
	return (AbstractAnalysisConfigurationModel)super.getModel();
    }
}
