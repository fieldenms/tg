package ua.com.fielden.platform.swing.review.report.centre;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.AnalysisType;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.report.analysis.configuration.AbstractAnalysisConfigurationView;
import ua.com.fielden.platform.swing.review.report.analysis.grid.configuration.GridConfigurationModel;
import ua.com.fielden.platform.swing.review.report.analysis.grid.configuration.GridConfigurationView;

public abstract class AbstractSingleAnalysisEntityCentre<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> extends AbstractEntityCentre<T, CDTME> {

    private static final long serialVersionUID = -7393061848126429375L;

    private int analysisCounter;

    private GridConfigurationView<T, CDTME> gridConfigurationView;

    public AbstractSingleAnalysisEntityCentre(final AbstractEntityCentreModel<T, CDTME> model, final BlockingIndefiniteProgressLayer progressLayer) {
	super(model, progressLayer);
	this.analysisCounter = 0;
	this.gridConfigurationView = null;
    }

    @Override
    public List<AbstractAnalysisConfigurationView<T, CDTME, ?, ?, ?, ?>> getAnalysisList() {
	final List<AbstractAnalysisConfigurationView<T, CDTME, ?, ?, ?, ?>> analysisList = new ArrayList<AbstractAnalysisConfigurationView<T,CDTME,?,?,?,?>>();
	analysisList.add(gridConfigurationView);
	return analysisList;
    }

    @Override
    public void addAnalysis(final String name, final AnalysisType analysisType) {
	if(name == null && analysisType == null && analysisCounter == 0){
	    final BlockingIndefiniteProgressLayer reviewProgressLayer = getReviewProgressLayer();
	    final GridConfigurationModel<T, CDTME> configModel = new GridConfigurationModel<T, CDTME>(getModel().getCriteria());
	    gridConfigurationView = new GridConfigurationView<T, CDTME>(configModel, this, reviewProgressLayer);
	    reviewProgressLayer.setView(gridConfigurationView);
	    gridConfigurationView.open();
	    setCurrentAnalysisConfigurationView(gridConfigurationView);
	    analysisCounter++;
	}else{
	    if(name != null || analysisType != null){
		throw new IllegalArgumentException("This centre can not have analysis different then main details analysis!");
	    }
	    if(analysisCounter > 1){
		throw new IllegalStateException("This centre may only one analysis!");
	    }
	}
    }

    @Override
    public void removeAnalysis(final String name) {
	throw new UnsupportedOperationException("The analysis can not be reomed from this entity centre!");
    }

    @Override
    public JComponent getReviewPanel() {
	return getReviewProgressLayer();
    }

    final void createReview() {
	addAnalysis(null, null);
    }

}
