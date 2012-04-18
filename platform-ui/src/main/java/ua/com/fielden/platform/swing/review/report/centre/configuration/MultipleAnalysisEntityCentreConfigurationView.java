package ua.com.fielden.platform.swing.review.report.centre.configuration;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.AnalysisType;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IAnalysisDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IPivotDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.report.centre.MultipleAnalysisEntityCentre;

public class MultipleAnalysisEntityCentreConfigurationView<T extends AbstractEntity<?>> extends CentreConfigurationView<T, MultipleAnalysisEntityCentre<T>> {

    private static final long serialVersionUID = -6434256458143463705L;

    public MultipleAnalysisEntityCentreConfigurationView(final CentreConfigurationModel<T> model, final BlockingIndefiniteProgressLayer progressLayer) {
	super(model, progressLayer);
    }

    @Override
    protected MultipleAnalysisEntityCentre<T> createConfigurableView() {
	final MultipleAnalysisEntityCentre<T> previousView = getPreviousView();
	final MultipleAnalysisEntityCentre<T> newView = new MultipleAnalysisEntityCentre<T>(getModel().createEntityCentreModel(), this);
	if(previousView != null){
	    final String selectedAnalysis = previousView.getCurrentAnalysisConfigurationView().getModel().getName();
	    selectAnalysisView(newView, selectedAnalysis);
	}
	return newView;
    }

    /**
     * Selects the specified analysis in the given {@link MultipleAnalysisEntityCentre} instance.
     * 
     * @param newView
     * @param selectedAnalysis
     */
    private void selectAnalysisView(final MultipleAnalysisEntityCentre<T> newView, final String selectedAnalysis) {
	final ICentreDomainTreeManager manager = newView.getModel().getCriteria().getCentreDomainTreeMangerAndEnhancer();
	final IAbstractAnalysisDomainTreeManager analysisManager = manager.getAnalysisManager(selectedAnalysis);
	if(analysisManager != null){
	    newView.addAnalysis(selectedAnalysis, determineAnalysisType(analysisManager));
	}
    }

    /**
     * Returns the analysis type for the specified instance of {@link IAbstractAnalysisDomainTreeManager}.
     * 
     * @param analysisManager
     * @return
     */
    private AnalysisType determineAnalysisType(final IAbstractAnalysisDomainTreeManager analysisManager) {
	if(analysisManager instanceof IAnalysisDomainTreeManager){
	    return AnalysisType.SIMPLE;
	}else if(analysisManager instanceof IPivotDomainTreeManager){
	    return AnalysisType.PIVOT;
	}
	return null;
    }
}
