package ua.com.fielden.platform.swing.review.report.centre.factory;

import java.util.Map;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.analysis.DetailsFrame;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.analysis.customiser.IAnalysisCustomiser;
import ua.com.fielden.platform.swing.review.report.analysis.grid.GridAnalysisView;
import ua.com.fielden.platform.swing.review.report.analysis.grid.configuration.GridConfigurationModel;
import ua.com.fielden.platform.swing.review.report.analysis.grid.configuration.GridConfigurationView;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;


public class DefaultGridAnalysisFactory<T extends AbstractEntity<?>> implements IAnalysisFactory<T, GridConfigurationView<T, ICentreDomainTreeManagerAndEnhancer>> {

    private IAnalysisCustomiser<GridAnalysisView<T,ICentreDomainTreeManagerAndEnhancer>> analysisCustomiser;

    @Override
    public GridConfigurationView<T, ICentreDomainTreeManagerAndEnhancer> createAnalysis(//
	    final AbstractEntityCentre<T, ICentreDomainTreeManagerAndEnhancer> owner, //
	    final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> criteria, //
	    final String name, //
	    final Map<Object, DetailsFrame> detailsCache,//
	    final BlockingIndefiniteProgressLayer progressLayer) {
	final GridConfigurationModel<T, ICentreDomainTreeManagerAndEnhancer> analysisModel = new GridConfigurationModel<T, ICentreDomainTreeManagerAndEnhancer>(criteria);
	return GridConfigurationView.createMainDetailsWithSpecificCustomiser(analysisModel, owner, analysisCustomiser, progressLayer);
    }

    @SuppressWarnings("unchecked")
    @Override
    public DefaultGridAnalysisFactory<T> setAnalysisCustomiser(final IAnalysisCustomiser<?> analysisCustomiser) {
	this.analysisCustomiser = (IAnalysisCustomiser<GridAnalysisView<T,ICentreDomainTreeManagerAndEnhancer>>)analysisCustomiser;
	return this;
    }


}
